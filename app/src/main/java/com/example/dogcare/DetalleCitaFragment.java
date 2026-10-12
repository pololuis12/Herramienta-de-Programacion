package com.example.dogcare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;

/**
 * Detalle de una cita y su ciclo de vida:
 * Pendiente → Confirmada → En atención → Atendida (o Cancelada / No asistió).
 */
public class DetalleCitaFragment extends Fragment {

    private static final String ARG_CITA_ID = "cita_id";
    private static final String CLINICA = "DogCare";
    /** Se avisa si se inicia la atención con más de esta anticipación. */
    private static final long ANTICIPACION_AVISO_MS = 60 * 60_000L;

    private DogCareRepository repo;
    private Cita cita;

    private TextView tvEstadoCita;
    private TextView tvInfoCita;
    private TextView tvAyudaEstado;
    private TextView tvResumenAtencion;
    private TextView tvHistorialCita;
    private LinearLayout llFormularioAtencion;
    private LinearLayout llAcciones;
    private EditText etDiagnostico;
    private EditText etTratamiento;
    private EditText etObservaciones;
    private EditText etCosto;

    public DetalleCitaFragment() {
        // Constructor vacío requerido
    }

    public static DetalleCitaFragment newInstance(String citaId) {
        DetalleCitaFragment fragment = new DetalleCitaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CITA_ID, citaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_cita, container, false);
        repo = new DogCareRepository(requireContext());

        tvEstadoCita = view.findViewById(R.id.tvEstadoCita);
        tvInfoCita = view.findViewById(R.id.tvInfoCita);
        tvAyudaEstado = view.findViewById(R.id.tvAyudaEstado);
        tvResumenAtencion = view.findViewById(R.id.tvResumenAtencion);
        tvHistorialCita = view.findViewById(R.id.tvHistorialCita);
        llFormularioAtencion = view.findViewById(R.id.llFormularioAtencion);
        llAcciones = view.findViewById(R.id.llAcciones);
        etDiagnostico = view.findViewById(R.id.etDiagnostico);
        etTratamiento = view.findViewById(R.id.etTratamiento);
        etObservaciones = view.findViewById(R.id.etObservaciones);
        etCosto = view.findViewById(R.id.etCosto);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnCitas);
        cita = repo.getCita(requireArguments().getString(ARG_CITA_ID));
        if (cita == null) {
            Toast.makeText(requireContext(), "La cita ya no existe", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }
        mostrarCita();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Guarda el avance del formulario para no perder lo escrito.
        if (cita != null && cita.estado == Cita.Estado.EN_ATENCION) {
            leerFormulario();
            repo.actualizarCita(cita);
        }
    }

    // ---------- Pantalla ----------

    private void mostrarCita() {
        tvEstadoCita.setText(cita.estado.etiqueta);
        tvEstadoCita.setBackgroundColor(ContextCompat.getColor(requireContext(), cita.estado.colorRes));

        tvInfoCita.setText("Mascota: " + cita.mascota
                + "\nServicio: " + cita.servicio
                + "\nFecha: " + cita.getFechaHoraTexto()
                + "\nCódigo: " + cita.codigo
                + textoRecordatorios(cita));
        tvHistorialCita.setText(cita.getHistorial());

        llFormularioAtencion.setVisibility(
                cita.estado == Cita.Estado.EN_ATENCION ? View.VISIBLE : View.GONE);
        tvResumenAtencion.setVisibility(
                cita.estado == Cita.Estado.ATENDIDA ? View.VISIBLE : View.GONE);

        llAcciones.removeAllViews();
        boolean yaPaso = cita.fechaHoraMillis < System.currentTimeMillis();

        switch (cita.estado) {
            case PENDIENTE:
                tvAyudaEstado.setText("La cita está reservada. Confírmala para asegurar el horario.");
                agregarBoton("Confirmar cita", R.color.green, this::confirmarCita);
                agregarBoton("Reprogramar", R.color.brown, this::reprogramar);
                agregarBoton("Cancelar cita", R.color.estado_cancelada, this::cancelarCita);
                break;

            case CONFIRMADA:
                tvAyudaEstado.setText("Cita confirmada. Cuando la mascota llegue, inicia la atención.");
                agregarBoton("Iniciar atención", R.color.estado_en_atencion, this::iniciarAtencion);
                agregarBoton("Enviar confirmación", R.color.green, () ->
                        compartir("Confirmación de cita", cita.getTextoConfirmacion(CLINICA)));
                agregarBoton("Reprogramar", R.color.brown, this::reprogramar);
                if (yaPaso) {
                    agregarBoton("Marcar como no asistió", R.color.estado_no_asistio,
                            this::marcarNoAsistio);
                }
                agregarBoton("Cancelar cita", R.color.estado_cancelada, this::cancelarCita);
                break;

            case EN_ATENCION:
                tvAyudaEstado.setText("Registra el diagnóstico y el tratamiento. "
                        + "El avance se guarda automáticamente.");
                etDiagnostico.setText(cita.diagnostico);
                etTratamiento.setText(cita.tratamiento);
                etObservaciones.setText(cita.observaciones);
                etCosto.setText(cita.costo);
                agregarBoton("Finalizar atención", R.color.green, this::finalizarAtencion);
                break;

            case ATENDIDA:
                tvAyudaEstado.setText("Atención finalizada. Quedó registrada en la historia clínica.");
                tvResumenAtencion.setText(cita.getTextoAtencion());
                agregarBoton("Compartir resumen", R.color.green, () ->
                        compartir("Resumen de atención", CLINICA + " — Resumen de atención de "
                                + cita.mascota + "\n\n" + cita.getTextoAtencion()));
                agregarBoton("Ver historia clínica", R.color.brown, () ->
                        ((MainActivity) requireActivity()).abrirEncima(new HistoriaClinicaFragment()));
                agregarBoton("Agendar control", R.color.beige, () ->
                        ((MainActivity) requireActivity()).navegarA(
                                CitasFragment.newInstance(cita.servicio)));
                break;

            case CANCELADA:
            case NO_ASISTIO:
                tvAyudaEstado.setText(cita.estado == Cita.Estado.CANCELADA
                        ? "Esta cita fue cancelada." : "La mascota no asistió a esta cita.");
                agregarBoton("Reagendar", R.color.green, () ->
                        ((MainActivity) requireActivity()).navegarA(
                                CitasFragment.newInstance(cita.servicio)));
                agregarBoton("Eliminar del historial", R.color.estado_cancelada, this::eliminarCita);
                break;
        }
    }

    private String textoRecordatorios(Cita cita) {
        List<Long> avisos = Recordatorios.proximosAvisos(requireContext(), cita);
        if (avisos.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\nRecordatorios: ");
        for (int i = 0; i < avisos.size(); i++) {
            sb.append(i == 0 ? "" : " y ").append(Cita.formatear(avisos.get(i)));
        }
        return sb.toString();
    }

    private void agregarBoton(String texto, int colorRes, Runnable accion) {
        MaterialButton boton = new MaterialButton(requireContext());
        boton.setText(texto);
        boton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), colorRes)));
        boton.setTextColor(ContextCompat.getColor(requireContext(),
                colorRes == R.color.beige ? R.color.black : R.color.white));
        boton.setOnClickListener(v -> accion.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        llAcciones.addView(boton, params);
    }

    private void guardarYMostrar(String mensaje) {
        repo.actualizarCita(cita);
        mostrarCita();
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    // ---------- Confirmación ----------

    private void confirmarCita() {
        if (cita.fechaHoraMillis < System.currentTimeMillis()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Horario vencido")
                    .setMessage("La fecha de esta cita ya pasó. Reprográmala antes de confirmarla.")
                    .setPositiveButton("Reprogramar", (d, w) -> reprogramar())
                    .setNegativeButton("Cerrar", null)
                    .show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar cita")
                .setMessage("¿Confirmar la cita de " + cita.mascota + " (" + cita.servicio + ") el "
                        + cita.getFechaHoraTexto() + "?")
                .setPositiveButton("Confirmar", (d, w) -> {
                    cita.estado = Cita.Estado.CONFIRMADA;
                    cita.confirmadaEn = System.currentTimeMillis();
                    guardarYMostrar("Cita confirmada — código " + cita.codigo);
                    ofrecerEnviarConfirmacion();
                })
                .setNegativeButton("Todavía no", null)
                .show();
    }

    private void ofrecerEnviarConfirmacion() {
        String telefono = repo.getTelefono();
        new AlertDialog.Builder(requireContext())
                .setTitle("Enviar comprobante")
                .setMessage(telefono.isEmpty()
                        ? "¿Quieres enviar el comprobante de la cita por WhatsApp, SMS o correo?"
                        : "¿Enviar el comprobante al dueño (" + telefono + ")?")
                .setPositiveButton("Enviar", (d, w) ->
                        compartir("Confirmación de cita", cita.getTextoConfirmacion(CLINICA)))
                .setNegativeButton("Ahora no", null)
                .show();
    }

    private void compartir(String titulo, String texto) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, titulo)
                .putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(intent, titulo));
    }

    private void reprogramar() {
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(Math.max(cita.fechaHoraMillis, System.currentTimeMillis()));

        DatePickerDialog fecha = new DatePickerDialog(requireContext(), (dp, anio, mes, dia) -> {
            TimePickerDialog hora = new TimePickerDialog(requireContext(), (tp, h, m) -> {
                Calendar nueva = Calendar.getInstance();
                nueva.set(anio, mes, dia, h, m, 0);
                nueva.set(Calendar.MILLISECOND, 0);
                aplicarReprogramacion(nueva.getTimeInMillis());
            }, actual.get(Calendar.HOUR_OF_DAY), actual.get(Calendar.MINUTE), true);
            hora.show();
        }, actual.get(Calendar.YEAR), actual.get(Calendar.MONTH), actual.get(Calendar.DAY_OF_MONTH));
        fecha.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        fecha.show();
    }

    private void aplicarReprogramacion(long nuevaFecha) {
        if (nuevaFecha <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Elige una fecha y hora futuras", Toast.LENGTH_LONG).show();
            return;
        }
        Cita conflicto = repo.buscarConflicto(nuevaFecha, cita.id);
        if (conflicto != null) {
            Toast.makeText(requireContext(), "Ese horario se cruza con otra cita ("
                    + conflicto.servicio + ", " + conflicto.getFechaHoraTexto() + ")",
                    Toast.LENGTH_LONG).show();
            return;
        }
        cita.fechaHoraMillis = nuevaFecha;
        // Un cambio de horario requiere volver a confirmar.
        cita.estado = Cita.Estado.PENDIENTE;
        cita.confirmadaEn = 0;
        guardarYMostrar("Cita reprogramada. Recuerda confirmarla de nuevo.");
    }

    private void cancelarCita() {
        EditText etMotivo = new EditText(requireContext());
        etMotivo.setHint("Motivo (opcional)");
        etMotivo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout contenedor = new LinearLayout(requireContext());
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(padding, 0, padding, 0);
        contenedor.addView(etMotivo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar cita")
                .setMessage("¿Cancelar la cita de " + cita.mascota + " del " + cita.getFechaHoraTexto() + "?")
                .setView(contenedor)
                .setPositiveButton("Sí, cancelar", (d, w) -> {
                    cita.estado = Cita.Estado.CANCELADA;
                    cita.canceladaEn = System.currentTimeMillis();
                    cita.motivoCancelacion = etMotivo.getText().toString().trim();
                    guardarYMostrar("Cita cancelada");
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void marcarNoAsistio() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No asistió")
                .setMessage("¿Registrar que " + cita.mascota + " no asistió a la cita?")
                .setPositiveButton("Registrar", (d, w) -> {
                    cita.estado = Cita.Estado.NO_ASISTIO;
                    cita.canceladaEn = System.currentTimeMillis();
                    guardarYMostrar("Inasistencia registrada");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- Atención ----------

    private void iniciarAtencion() {
        long anticipacion = cita.fechaHoraMillis - System.currentTimeMillis();
        if (anticipacion > ANTICIPACION_AVISO_MS) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cita programada para después")
                    .setMessage("Esta cita es el " + cita.getFechaHoraTexto()
                            + ". ¿Iniciar la atención ahora de todos modos?")
                    .setPositiveButton("Iniciar", (d, w) -> aplicarInicioAtencion())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            aplicarInicioAtencion();
        }
    }

    private void aplicarInicioAtencion() {
        cita.estado = Cita.Estado.EN_ATENCION;
        cita.inicioAtencion = System.currentTimeMillis();
        guardarYMostrar("Atención iniciada");
    }

    private void leerFormulario() {
        cita.diagnostico = etDiagnostico.getText().toString().trim();
        cita.tratamiento = etTratamiento.getText().toString().trim();
        cita.observaciones = etObservaciones.getText().toString().trim();
        cita.costo = etCosto.getText().toString().trim();
    }

    private void finalizarAtencion() {
        leerFormulario();
        boolean valido = true;
        if (cita.diagnostico.isEmpty()) {
            etDiagnostico.setError("Obligatorio");
            valido = false;
        }
        if (cita.tratamiento.isEmpty()) {
            etTratamiento.setError("Obligatorio");
            valido = false;
        }
        if (!valido) {
            repo.actualizarCita(cita);
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Finalizar atención")
                .setMessage("La atención se agregará a la historia clínica de " + cita.mascota
                        + " y ya no se podrá editar. ¿Continuar?")
                .setPositiveButton("Finalizar", (d, w) -> {
                    cita.estado = Cita.Estado.ATENDIDA;
                    cita.finAtencion = System.currentTimeMillis();
                    guardarYMostrar("Atención registrada en la historia clínica");
                })
                .setNegativeButton("Seguir editando", null)
                .show();
    }

    private void eliminarCita() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar del historial")
                .setMessage("¿Eliminar definitivamente esta cita?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repo.eliminarCita(cita.id);
                    cita = null;
                    Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
