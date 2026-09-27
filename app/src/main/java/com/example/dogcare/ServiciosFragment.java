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

    private Button btnConsulta;
    private Button btnVacunacion;
    private Button btnBanoPeluqueria;
    private Button btnCirugiaMenor;
    private Button btnDesparasitacion;
    private TextView tvServicioDetalle;

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
        tvServicioDetalle = view.findViewById(R.id.tvServicioDetalle);

        btnConsulta.setOnClickListener(v -> mostrarDetalleServicio(
                "Consulta general",
                "Valoración clínica del estado de salud de la mascota."));

        btnVacunacion.setOnClickListener(v -> mostrarDetalleServicio(
                "Vacunación",
                "Aplicación de vacunas según el esquema recomendado para la edad y especie."));

        btnBanoPeluqueria.setOnClickListener(v -> mostrarDetalleServicio(
                "Baño y peluquería",
                "Baño, corte y cuidado estético de la mascota."));

        btnCirugiaMenor.setOnClickListener(v -> mostrarDetalleServicio(
                "Cirugía menor",
                "Procedimientos quirúrgicos ambulatorios de baja complejidad."));

        btnDesparasitacion.setOnClickListener(v -> mostrarDetalleServicio(
                "Desparasitación",
                "Tratamiento antiparasitario interno y externo."));

        return view;
    }

    private void mostrarDetalleServicio(String nombre, String descripcion) {
        tvServicioDetalle.setText(nombre + ": " + descripcion);
    }
}
