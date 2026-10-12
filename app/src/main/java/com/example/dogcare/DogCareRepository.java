package com.example.dogcare;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Guarda los datos de la aplicación (perfil, fotos y citas) en SharedPreferences.
 */
public class DogCareRepository {

    private static final String PREFS = "dogcare_prefs";

    private static final String KEY_NOMBRE = "perfil_nombre";
    private static final String KEY_RAZA = "perfil_raza";
    private static final String KEY_EDAD = "perfil_edad";
    private static final String KEY_PESO = "perfil_peso";
    private static final String KEY_DUENO = "perfil_dueno";
    private static final String KEY_TELEFONO = "perfil_telefono";
    private static final String KEY_FOTO_PERFIL = "perfil_foto";
    private static final String KEY_DESCRIPCION = "perfil_descripcion";
    private static final String KEY_FOTOS = "fotos";
    private static final String KEY_FOTOS_DESCRIPCION = "fotos_descripcion";
    private static final String KEY_CITAS = "citas";
    private static final String KEY_RECORDATORIOS = "recordatorios_activos";

    /** Tiempo mínimo entre dos citas activas. */
    public static final int MINUTOS_ENTRE_CITAS = 30;

    private final Context context;
    private final SharedPreferences prefs;

    public DogCareRepository(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------- Perfil ----------

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "Lucas");
    }

    public String getRaza() {
        return prefs.getString(KEY_RAZA, "Golden Retriever");
    }

    public String getEdad() {
        return prefs.getString(KEY_EDAD, "3 años");
    }

    public String getPeso() {
        return prefs.getString(KEY_PESO, "30 kg");
    }

    public String getDueno() {
        return prefs.getString(KEY_DUENO, "");
    }

    public String getTelefono() {
        return prefs.getString(KEY_TELEFONO, "");
    }

    public void guardarPerfil(String nombre, String raza, String edad, String peso,
                              String dueno, String telefono) {
        prefs.edit()
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_RAZA, raza)
                .putString(KEY_EDAD, edad)
                .putString(KEY_PESO, peso)
                .putString(KEY_DUENO, dueno)
                .putString(KEY_TELEFONO, telefono)
                .apply();
    }

    /** Texto largo del perfil: temperamento, alimentación, antecedentes, etc. */
    public String getDescripcion() {
        return prefs.getString(KEY_DESCRIPCION, DESCRIPCION_INICIAL);
    }

    public void setDescripcion(String descripcion) {
        prefs.edit().putString(KEY_DESCRIPCION, descripcion).apply();
    }

    private static final String DESCRIPCION_INICIAL =
            "Temperamento: juguetón, sociable con otros perros y muy apegado a la familia.\n\n"
                    + "Alimentación: concentrado para adulto de raza grande, dos raciones al día "
                    + "(mañana y noche). Sin alergias alimentarias conocidas.\n\n"
                    + "Vacunas: esquema completo (moquillo, parvovirus, hepatitis, rabia). "
                    + "Refuerzo anual pendiente según el calendario de la clínica.\n\n"
                    + "Desparasitación: interna cada 3 meses y externa mensual.\n\n"
                    + "Antecedentes: otitis leve tratada a los 2 años. "
                    + "Sin cirugías ni enfermedades crónicas.\n\n"
                    + "Actividad: dos paseos diarios de 30 minutos; le gusta nadar y jugar con pelota.";

    public String getFotoPerfil() {
        return prefs.getString(KEY_FOTO_PERFIL, null);
    }

    public void setFotoPerfil(String uri) {
        prefs.edit().putString(KEY_FOTO_PERFIL, uri).apply();
    }

    // ---------- Fotos ----------

    public List<String> getFotos() {
        List<String> fotos = new ArrayList<>();
        JSONArray array = leerArray(KEY_FOTOS);
        for (int i = 0; i < array.length(); i++) {
            fotos.add(array.optString(i));
        }
        return fotos;
    }

    public void agregarFoto(String uri) {
        List<String> fotos = getFotos();
        if (!fotos.contains(uri)) {
            fotos.add(0, uri);
            prefs.edit().putString(KEY_FOTOS, new JSONArray(fotos).toString()).apply();
        }
    }

    public void eliminarFoto(String uri) {
        List<String> fotos = getFotos();
        fotos.remove(uri);
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_FOTOS, new JSONArray(fotos).toString());
        if (uri.equals(getFotoPerfil())) {
            editor.remove(KEY_FOTO_PERFIL);
        }
        JSONObject descripciones = leerObjeto(KEY_FOTOS_DESCRIPCION);
        descripciones.remove(uri);
        editor.putString(KEY_FOTOS_DESCRIPCION, descripciones.toString());
        editor.apply();
    }

    public String getDescripcionFoto(String uri) {
        return leerObjeto(KEY_FOTOS_DESCRIPCION).optString(uri, "");
    }

    public void setDescripcionFoto(String uri, String descripcion) {
        JSONObject descripciones = leerObjeto(KEY_FOTOS_DESCRIPCION);
        try {
            descripciones.put(uri, descripcion);
        } catch (JSONException ignored) {
            return;
        }
        prefs.edit().putString(KEY_FOTOS_DESCRIPCION, descripciones.toString()).apply();
    }

    // ---------- Citas ----------

    public List<Cita> getCitas() {
        List<Cita> citas = new ArrayList<>();
        JSONArray array = leerArray(KEY_CITAS);
        for (int i = 0; i < array.length(); i++) {
            if (array.optJSONObject(i) != null) {
                citas.add(Cita.fromJson(array.optJSONObject(i)));
            }
        }
        Collections.sort(citas, Comparator.comparingLong(c -> c.fechaHoraMillis));
        return citas;
    }

    public Cita getCita(String id) {
        for (Cita cita : getCitas()) {
            if (cita.id.equals(id)) {
                return cita;
            }
        }
        return null;
    }

    /** Devuelve la próxima cita activa (pendiente o confirmada) futura, o null. */
    public Cita getProximaCita() {
        long ahora = System.currentTimeMillis();
        for (Cita cita : getCitas()) {
            if (cita.fechaHoraMillis >= ahora
                    && (cita.estado == Cita.Estado.PENDIENTE || cita.estado == Cita.Estado.CONFIRMADA)) {
                return cita;
            }
        }
        return null;
    }

    /** Citas atendidas, de la más reciente a la más antigua: la historia clínica. */
    public List<Cita> getHistoriaClinica() {
        List<Cita> atendidas = new ArrayList<>();
        for (Cita cita : getCitas()) {
            if (cita.estado == Cita.Estado.ATENDIDA) {
                atendidas.add(cita);
            }
        }
        Collections.reverse(atendidas);
        return atendidas;
    }

    /**
     * Indica si ya existe otra cita activa a menos de {@link #MINUTOS_ENTRE_CITAS} minutos
     * del horario indicado. {@code excluirId} permite ignorar la propia cita al reprogramar.
     */
    public Cita buscarConflicto(long fechaHoraMillis, String excluirId) {
        long margen = MINUTOS_ENTRE_CITAS * 60_000L;
        for (Cita cita : getCitas()) {
            if (cita.estado.esActiva() && !cita.id.equals(excluirId)
                    && Math.abs(cita.fechaHoraMillis - fechaHoraMillis) < margen) {
                return cita;
            }
        }
        return null;
    }

    public void agregarCita(Cita cita) {
        List<Cita> citas = getCitas();
        citas.add(cita);
        guardarCitas(citas);
        Recordatorios.sincronizar(context, cita);
    }

    public void actualizarCita(Cita actualizada) {
        List<Cita> citas = getCitas();
        for (int i = 0; i < citas.size(); i++) {
            if (citas.get(i).id.equals(actualizada.id)) {
                citas.set(i, actualizada);
                guardarCitas(citas);
                Recordatorios.sincronizar(context, actualizada);
                return;
            }
        }
    }

    public void eliminarCita(String id) {
        List<Cita> citas = getCitas();
        citas.removeIf(c -> c.id.equals(id));
        guardarCitas(citas);
        Recordatorios.cancelar(context, id);
    }

    // ---------- Recordatorios ----------

    public boolean getRecordatoriosActivos() {
        return prefs.getBoolean(KEY_RECORDATORIOS, true);
    }

    public void setRecordatoriosActivos(boolean activos) {
        prefs.edit().putBoolean(KEY_RECORDATORIOS, activos).commit();
        Recordatorios.reprogramarTodas(context);
    }

    private void guardarCitas(List<Cita> citas) {
        JSONArray array = new JSONArray();
        try {
            for (Cita cita : citas) {
                array.put(cita.toJson());
            }
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(KEY_CITAS, array.toString()).apply();
    }

    private JSONObject leerObjeto(String key) {
        try {
            return new JSONObject(prefs.getString(key, "{}"));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private JSONArray leerArray(String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
}
