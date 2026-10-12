package com.example.dogcare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class Cita {

    /** Ciclo de vida de una cita: Pendiente → Confirmada → En atención → Atendida. */
    public enum Estado {
        PENDIENTE("Pendiente de confirmar", R.color.estado_pendiente),
        CONFIRMADA("Confirmada", R.color.estado_confirmada),
        EN_ATENCION("En atención", R.color.estado_en_atencion),
        ATENDIDA("Atendida", R.color.estado_atendida),
        CANCELADA("Cancelada", R.color.estado_cancelada),
        NO_ASISTIO("No asistió", R.color.estado_no_asistio);

        public final String etiqueta;
        public final int colorRes;

        Estado(String etiqueta, int colorRes) {
            this.etiqueta = etiqueta;
            this.colorRes = colorRes;
        }

        /** Las citas activas ocupan horario en la agenda. */
        public boolean esActiva() {
            return this == PENDIENTE || this == CONFIRMADA || this == EN_ATENCION;
        }
    }

    private static final String CODIGO_LEGADO = "DC-0000";

    public final String id;
    public final String codigo;
    public final String mascota;
    public final String servicio;
    public final long creadaEn;

    public long fechaHoraMillis;
    public Estado estado = Estado.PENDIENTE;

    public long confirmadaEn;
    public long inicioAtencion;
    public long finAtencion;
    public long canceladaEn;
    public String motivoCancelacion = "";

    // Datos de la atención (historia clínica).
    public String diagnostico = "";
    public String tratamiento = "";
    public String observaciones = "";
    public String costo = "";

    public Cita(String mascota, String servicio, long fechaHoraMillis) {
        this(UUID.randomUUID().toString(),
                String.format(Locale.ROOT, "DC-%04d", new Random().nextInt(9999) + 1),
                mascota, servicio, System.currentTimeMillis());
        this.fechaHoraMillis = fechaHoraMillis;
    }

    private Cita(String id, String codigo, String mascota, String servicio, long creadaEn) {
        this.id = id;
        this.codigo = codigo;
        this.mascota = mascota;
        this.servicio = servicio;
        this.creadaEn = creadaEn;
    }

    public static String formatear(long millis) {
        return String.format(Locale.getDefault(), "%1$td/%1$tm/%1$tY %1$tH:%1$tM", millis);
    }

    public String getFechaHoraTexto() {
        return String.format(Locale.getDefault(), "%1$td/%1$tm/%1$tY a las %1$tH:%1$tM", fechaHoraMillis);
    }

    public String getResumen() {
        return mascota + " — " + servicio + "\n" + getFechaHoraTexto();
    }

    /** Línea de tiempo de los cambios de estado de la cita. */
    public String getHistorial() {
        StringBuilder sb = new StringBuilder();
        if (creadaEn > 0) {
            sb.append("\n• Agendada: ").append(formatear(creadaEn));
        }
        if (confirmadaEn > 0) {
            sb.append("\n• Confirmada: ").append(formatear(confirmadaEn));
        }
        if (inicioAtencion > 0) {
            sb.append("\n• Atención iniciada: ").append(formatear(inicioAtencion));
        }
        if (finAtencion > 0) {
            sb.append("\n• Atención finalizada: ").append(formatear(finAtencion));
        }
        if (canceladaEn > 0) {
            sb.append("\n• ").append(estado == Estado.NO_ASISTIO ? "Marcada como no asistió: " : "Cancelada: ")
                    .append(formatear(canceladaEn));
            if (!motivoCancelacion.isEmpty()) {
                sb.append(" (").append(motivoCancelacion).append(")");
            }
        }
        return sb.length() == 0 ? "Sin registros." : sb.substring(1);
    }

    public String getTextoConfirmacion(String clinica) {
        return "✅ " + clinica + " — Cita confirmada\n"
                + "Mascota: " + mascota + "\n"
                + "Servicio: " + servicio + "\n"
                + "Fecha: " + getFechaHoraTexto() + "\n"
                + "Código: " + codigo + "\n"
                + "Por favor llega 10 minutos antes.";
    }

    public String getTextoAtencion() {
        StringBuilder sb = new StringBuilder();
        sb.append(servicio).append(" — ").append(formatear(finAtencion > 0 ? finAtencion : fechaHoraMillis))
                .append(" (").append(codigo).append(")")
                .append("\nDiagnóstico: ").append(diagnostico)
                .append("\nTratamiento: ").append(tratamiento);
        if (!observaciones.isEmpty()) {
            sb.append("\nObservaciones: ").append(observaciones);
        }
        if (!costo.isEmpty()) {
            sb.append("\nCosto: ").append(getCostoTexto());
        }
        return sb.toString();
    }

    public String getCostoTexto() {
        try {
            return String.format(Locale.getDefault(), "$%,.0f", Double.parseDouble(costo));
        } catch (NumberFormatException e) {
            return "$" + costo;
        }
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("id", id)
                .put("codigo", codigo)
                .put("mascota", mascota)
                .put("servicio", servicio)
                .put("creadaEn", creadaEn)
                .put("fechaHora", fechaHoraMillis)
                .put("estado", estado.name())
                .put("confirmadaEn", confirmadaEn)
                .put("inicioAtencion", inicioAtencion)
                .put("finAtencion", finAtencion)
                .put("canceladaEn", canceladaEn)
                .put("motivoCancelacion", motivoCancelacion)
                .put("diagnostico", diagnostico)
                .put("tratamiento", tratamiento)
                .put("observaciones", observaciones)
                .put("costo", costo);
    }

    static Cita fromJson(JSONObject json) {
        long fechaHora = json.optLong("fechaHora");
        String id = json.optString("id");
        // Las citas guardadas antes de existir el código ("DC-0000" en una versión previa)
        // reciben uno estable derivado del id; su fecha de creación es desconocida.
        String codigo = json.optString("codigo", CODIGO_LEGADO);
        boolean legado = CODIGO_LEGADO.equals(codigo);
        if (legado) {
            codigo = String.format(Locale.ROOT, "DC-%04d", Math.abs(id.hashCode() % 10000));
        }
        Cita cita = new Cita(
                id,
                codigo,
                json.optString("mascota"),
                json.optString("servicio"),
                legado ? 0 : json.optLong("creadaEn"));
        cita.fechaHoraMillis = fechaHora;
        try {
            cita.estado = Estado.valueOf(json.optString("estado", Estado.PENDIENTE.name()));
        } catch (IllegalArgumentException e) {
            cita.estado = Estado.PENDIENTE;
        }
        cita.confirmadaEn = json.optLong("confirmadaEn");
        cita.inicioAtencion = json.optLong("inicioAtencion");
        cita.finAtencion = json.optLong("finAtencion");
        cita.canceladaEn = json.optLong("canceladaEn");
        cita.motivoCancelacion = json.optString("motivoCancelacion");
        cita.diagnostico = json.optString("diagnostico");
        cita.tratamiento = json.optString("tratamiento");
        cita.observaciones = json.optString("observaciones");
        cita.costo = json.optString("costo");
        return cita;
    }
}
