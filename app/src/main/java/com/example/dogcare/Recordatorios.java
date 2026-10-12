package com.example.dogcare;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Programa los recordatorios de las citas: uno 24 horas antes y otro 1 hora antes.
 * Solo las citas pendientes o confirmadas generan recordatorios.
 */
public final class Recordatorios {

    static final String CANAL_ID = "recordatorios_citas";

    static final String ACCION_RECORDATORIO = "com.example.dogcare.RECORDATORIO";
    static final String ACCION_CONFIRMAR = "com.example.dogcare.CONFIRMAR_CITA";

    static final String EXTRA_CITA_ID = "cita_id";
    static final String EXTRA_TIPO = "tipo";
    static final String EXTRA_FECHA_CITA = "fecha_cita";

    static final int TIPO_DIA_ANTES = 0;
    static final int TIPO_HORA_ANTES = 1;
    private static final long[] ANTICIPACION_MS = {24 * 60 * 60_000L, 60 * 60_000L};
    private static final long VENTANA_INEXACTA_MS = 10 * 60_000L;

    private Recordatorios() {
    }

    public static void crearCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(CANAL_ID, "Recordatorios de citas",
                NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription("Avisos 24 horas y 1 hora antes de cada cita.");
        context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    public static boolean tienePermiso(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean puedeUsarAlarmasExactas(Context context) {
        return context.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }

    static boolean debeRecordarse(Cita cita) {
        return cita.estado == Cita.Estado.PENDIENTE || cita.estado == Cita.Estado.CONFIRMADA;
    }

    /** Momentos futuros en los que sonarán los recordatorios de la cita. */
    public static List<Long> proximosAvisos(Context context, Cita cita) {
        List<Long> avisos = new ArrayList<>();
        if (!new DogCareRepository(context).getRecordatoriosActivos() || !debeRecordarse(cita)) {
            return avisos;
        }
        long ahora = System.currentTimeMillis();
        for (long anticipacion : ANTICIPACION_MS) {
            long momento = cita.fechaHoraMillis - anticipacion;
            if (momento > ahora) {
                avisos.add(momento);
            }
        }
        return avisos;
    }

    /** Deja los recordatorios de la cita acordes a su estado y horario actuales. */
    public static void sincronizar(Context context, Cita cita) {
        cancelar(context, cita.id);
        if (!new DogCareRepository(context).getRecordatoriosActivos() || !debeRecordarse(cita)) {
            return;
        }
        AlarmManager alarmas = context.getSystemService(AlarmManager.class);
        long ahora = System.currentTimeMillis();
        for (int tipo = 0; tipo < ANTICIPACION_MS.length; tipo++) {
            long momento = cita.fechaHoraMillis - ANTICIPACION_MS[tipo];
            if (momento > ahora) {
                PendingIntent alarma = crearPendingIntent(context, cita, tipo);
                if (alarmas.canScheduleExactAlarms()) {
                    alarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, alarma);
                } else {
                    // Sin permiso de alarmas exactas Android puede retrasar una alarma inexacta
                    // hasta una hora; con una ventana el retraso máximo queda acotado.
                    alarmas.setWindow(AlarmManager.RTC_WAKEUP, momento, VENTANA_INEXACTA_MS, alarma);
                }
            }
        }
    }

    public static void cancelar(Context context, String citaId) {
        AlarmManager alarmas = context.getSystemService(AlarmManager.class);
        for (int tipo = 0; tipo < ANTICIPACION_MS.length; tipo++) {
            PendingIntent existente = PendingIntent.getBroadcast(context, 0,
                    crearIntent(context, citaId, tipo),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (existente != null) {
                alarmas.cancel(existente);
                existente.cancel();
            }
        }
        context.getSystemService(NotificationManager.class).cancel(idNotificacion(citaId));
    }

    /** Vuelve a programar todas las citas (al abrir la app, tras reiniciar o cambiar la hora). */
    public static void reprogramarTodas(Context context) {
        for (Cita cita : new DogCareRepository(context).getCitas()) {
            sincronizar(context, cita);
        }
    }

    static int idNotificacion(String citaId) {
        return citaId.hashCode();
    }

    private static Intent crearIntent(Context context, String citaId, int tipo) {
        // El Uri hace único cada PendingIntent (una alarma por cita y por tipo).
        return new Intent(context, RecordatorioReceiver.class)
                .setAction(ACCION_RECORDATORIO)
                .setData(Uri.parse("dogcare://recordatorio/" + citaId + "/" + tipo));
    }

    private static PendingIntent crearPendingIntent(Context context, Cita cita, int tipo) {
        Intent intent = crearIntent(context, cita.id, tipo)
                .putExtra(EXTRA_CITA_ID, cita.id)
                .putExtra(EXTRA_TIPO, tipo)
                .putExtra(EXTRA_FECHA_CITA, cita.fechaHoraMillis);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
