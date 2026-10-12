package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ServiciosFragment extends Fragment {

    private static final String STATE_SERVICIO = "servicio_seleccionado";

    private Button btnConsulta;
    private Button btnVacunacion;
    private Button btnBanoPeluqueria;
    private Button btnCirugiaMenor;
    private Button btnDesparasitacion;
    private Button btnAgendarServicio;
    private TextView tvServicioDetalle;

    private String servicioSeleccionado;

    public ServiciosFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servicios, container, false);

        btnConsulta = view.findViewById(R.id.btnConsulta);
        btnVacunacion = view.findViewById(R.id.btnVacunacion);
        btnBanoPeluqueria = view.findViewById(R.id.btnBanoPeluqueria);
        btnCirugiaMenor = view.findViewById(R.id.btnCirugiaMenor);
        btnDesparasitacion = view.findViewById(R.id.btnDesparasitacion);
        btnAgendarServicio = view.findViewById(R.id.btnAgendarServicio);
        tvServicioDetalle = view.findViewById(R.id.tvServicioDetalle);

        btnConsulta.setOnClickListener(v -> mostrarDetalleServicio("Consulta general"));
        btnVacunacion.setOnClickListener(v -> mostrarDetalleServicio("Vacunación"));
        btnBanoPeluqueria.setOnClickListener(v -> mostrarDetalleServicio("Baño y peluquería"));
        btnCirugiaMenor.setOnClickListener(v -> mostrarDetalleServicio("Cirugía menor"));
        btnDesparasitacion.setOnClickListener(v -> mostrarDetalleServicio("Desparasitación"));

        btnAgendarServicio.setOnClickListener(v -> ((MainActivity) requireActivity())
                .navegarA(CitasFragment.newInstance(servicioSeleccionado)));

        if (savedInstanceState != null && savedInstanceState.getString(STATE_SERVICIO) != null) {
            mostrarDetalleServicio(savedInstanceState.getString(STATE_SERVICIO));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnServicios);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SERVICIO, servicioSeleccionado);
    }

    private void mostrarDetalleServicio(String nombre) {
        servicioSeleccionado = nombre;
        tvServicioDetalle.setText(nombre + ": " + descripcion(nombre));
        btnAgendarServicio.setText("Agendar " + nombre.toLowerCase());
        btnAgendarServicio.setVisibility(View.VISIBLE);
    }

    private String descripcion(String nombre) {
        switch (nombre) {
            case "Consulta general":
                return "Valoración clínica del estado de salud de la mascota.";
            case "Vacunación":
                return "Aplicación de vacunas según el esquema recomendado para la edad y especie.";
            case "Baño y peluquería":
                return "Baño, corte y cuidado estético de la mascota.";
            case "Cirugía menor":
                return "Procedimientos quirúrgicos ambulatorios de baja complejidad.";
            case "Desparasitación":
                return "Tratamiento antiparasitario interno y externo.";
            default:
                return "";
        }
    }
}
