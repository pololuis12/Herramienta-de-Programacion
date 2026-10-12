package com.example.dogcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

/**
 * Historia clínica de la mascota: datos del perfil y todas las atenciones finalizadas.
 */
public class HistoriaClinicaFragment extends Fragment {

    private DogCareRepository repo;

    private TextView tvDatosMascota;
    private TextView tvEstadisticas;
    private TextView tvSinHistoria;
    private LinearLayout llHistoria;

    public HistoriaClinicaFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historia_clinica, container, false);
        repo = new DogCareRepository(requireContext());

        tvDatosMascota = view.findViewById(R.id.tvDatosMascota);
        tvEstadisticas = view.findViewById(R.id.tvEstadisticas);
        tvSinHistoria = view.findViewById(R.id.tvSinHistoria);
        llHistoria = view.findViewById(R.id.llHistoria);

        view.findViewById(R.id.btnCompartirHistoria).setOnClickListener(v -> compartirHistoria());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnPerfil);
        mostrarHistoria();
    }

    private String datosMascota() {
        String texto = "Paciente: " + repo.getNombre()
                + "\nRaza: " + repo.getRaza()
                + "\nEdad: " + repo.getEdad() + " · Peso: " + repo.getPeso();
        if (!repo.getDueno().isEmpty()) {
            texto += "\nDueño: " + repo.getDueno();
        }
        if (!repo.getTelefono().isEmpty()) {
            texto += "\nTeléfono: " + repo.getTelefono();
        }
        return texto;
    }

    private void mostrarHistoria() {
        tvDatosMascota.setText(datosMascota());

        List<Cita> atenciones = repo.getHistoriaClinica();
        int cancelaciones = 0;
        int inasistencias = 0;
        for (Cita c : repo.getCitas()) {
            if (c.estado == Cita.Estado.CANCELADA) cancelaciones++;
            if (c.estado == Cita.Estado.NO_ASISTIO) inasistencias++;
        }
        double totalCosto = 0;
        for (Cita c : atenciones) {
            try {
                totalCosto += Double.parseDouble(c.costo);
            } catch (NumberFormatException ignored) {
                // Costo vacío o no numérico.
            }
        }

        StringBuilder stats = new StringBuilder();
        stats.append("Atenciones: ").append(atenciones.size());
        if (!atenciones.isEmpty()) {
            stats.append(" · Última visita: ").append(Cita.formatear(atenciones.get(0).finAtencion));
            stats.append("\nTotal invertido: $").append(String.format(Locale.getDefault(), "%,.0f", totalCosto));
        }
        stats.append("\nCanceladas: ").append(cancelaciones)
                .append(" · Inasistencias: ").append(inasistencias);
        tvEstadisticas.setText(stats.toString());

        llHistoria.removeAllViews();
        tvSinHistoria.setVisibility(atenciones.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Cita c : atenciones) {
            View fila = inflater.inflate(R.layout.item_historia, llHistoria, false);
            ((TextView) fila.findViewById(R.id.tvHistoriaTitulo))
                    .setText(Cita.formatear(c.finAtencion) + " — " + c.servicio);
            StringBuilder detalle = new StringBuilder()
                    .append("Diagnóstico: ").append(c.diagnostico)
                    .append("\nTratamiento: ").append(c.tratamiento);
            if (!c.observaciones.isEmpty()) {
                detalle.append("\nObservaciones: ").append(c.observaciones);
            }
            if (!c.costo.isEmpty()) {
                detalle.append("\nCosto: ").append(c.getCostoTexto());
            }
            ((TextView) fila.findViewById(R.id.tvHistoriaDetalle)).setText(detalle.toString());
            fila.setOnClickListener(v ->
                    ((MainActivity) requireActivity()).abrirEncima(DetalleCitaFragment.newInstance(c.id)));
            llHistoria.addView(fila);
        }
    }

    private void compartirHistoria() {
        List<Cita> atenciones = repo.getHistoriaClinica();
        if (atenciones.isEmpty()) {
            Toast.makeText(requireContext(), "No hay atenciones para compartir", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder texto = new StringBuilder("DogCare — Historia clínica\n\n")
                .append(datosMascota()).append("\n");
        for (Cita c : atenciones) {
            texto.append("\n").append(c.getTextoAtencion()).append("\n");
        }
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Historia clínica de " + repo.getNombre())
                .putExtra(Intent.EXTRA_TEXT, texto.toString());
        startActivity(Intent.createChooser(intent, "Compartir historia clínica"));
    }
}
