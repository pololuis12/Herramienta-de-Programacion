package com.example.dogcare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

/**
 * Muestra la notificación de recordatorio y atiende el botón "Confirmar" de la notificación.
 */
public class RecordatorioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String citaId = intent.getStringExtra(Recordatorios.EXTRA_CITA_ID);
        if (citaId == null) {
            return;
        }
        DogCareRepository repo = new DogCareRepository(context);
        Cita cita = repo.getCita(citaId);

        if (Recordatorios.ACCION_CONFIRMAR.equals(intent.getAction())) {
            confirmar(context, repo, cita);
            return;
        }

        long fechaEsperada = intent.getLongExtra(Recordatorios.EXTRA_FECHA_CITA, 0);
        // Se ignora si la cita se eliminó, cambió de estado o se reprogramó desde que se creó la alarma.
        if (cita == null || !Recordatorios.debeRecordarse(cita)
                || cita.fechaHoraMillis != fechaEsperada
                || !repo.getRecordatoriosActivos() || !Recordatorios.tienePermiso(context)) {
            return;
        }
        mostrarNotificacion(context, cita, intent.getIntExtra(Recordatorios.EXTRA_TIPO, 0));
    }

    private void mostrarNotificacion(Context context, Cita cita, int tipo) {
        Recordatorios.crearCanal(context);
        String cuando = tipo == Recordatorios.TIPO_HORA_ANTES ? "en 1 hora" : "mañana";
        String texto = cita.servicio + " — " + cita.getFechaHoraTexto() + " · Código " + cita.codigo;
        if (cita.estado == Cita.Estado.PENDIENTE) {
            texto += "\nAún no está confirmada.";
        }

        Intent abrir = new Intent(context, MainActivity.class)
                .setData(Uri.parse("dogcare://cita/" + cita.id))
                .putExtra(MainActivity.EXTRA_ABRIR_CITA, cita.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent alTocar = PendingIntent.getActivity(context, 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Recordatorios.CANAL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setColor(context.getColor(R.color.green))
                .setContentTitle("Cita de " + cita.mascota + " " + cuando)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(alTocar)
                .setAutoCancel(true)
                .addAction(0, "Ver cita", alTocar);

        if (cita.estado == Cita.Estado.PENDIENTE) {
            Intent confirmar = new Intent(context, RecordatorioReceiver.class)
                    .setAction(Recordatorios.ACCION_CONFIRMAR)
                    .setData(Uri.parse("dogcare://confirmar/" + cita.id))
                    .putExtra(Recordatorios.EXTRA_CITA_ID, cita.id);
            builder.addAction(0, "Confirmar", PendingIntent.getBroadcast(context, 0, confirmar,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }

        context.getSystemService(NotificationManager.class)
                .notify(Recordatorios.idNotificacion(cita.id), builder.build());
    }

    private void confirmar(Context context, DogCareRepository repo, Cita cita) {
        if (cita == null || cita.estado != Cita.Estado.PENDIENTE) {
            return;
        }
        cita.estado = Cita.Estado.CONFIRMADA;
        cita.confirmadaEn = System.currentTimeMillis();
        // actualizarCita vuelve a sincronizar los recordatorios y retira la notificación actual.
        repo.actualizarCita(cita);

        if (Recordatorios.tienePermiso(context)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Recordatorios.CANAL_ID)
                    .setSmallIcon(R.drawable.ic_notificacion)
                    .setColor(context.getColor(R.color.green))
                    .setContentTitle("Cita confirmada")
                    .setContentText(cita.servicio + " de " + cita.mascota + " — " + cita.getFechaHoraTexto())
                    .setTimeoutAfter(5_000)
                    .setAutoCancel(true);
            context.getSystemService(NotificationManager.class)
                    .notify(Recordatorios.idNotificacion(cita.id), builder.build());
        }
    }
}
