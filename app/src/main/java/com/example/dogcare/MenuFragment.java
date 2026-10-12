package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    private Button btnPerfil;
    private Button btnFotos;
    private Button btnServicios;
    private Button btnCitas;

    private int seleccionId = R.id.btnPerfil;

    public MenuFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        btnPerfil = view.findViewById(R.id.btnPerfil);
        btnFotos = view.findViewById(R.id.btnFotos);
        btnServicios = view.findViewById(R.id.btnServicios);
        btnCitas = view.findViewById(R.id.btnCitas);

        btnPerfil.setOnClickListener(v -> navegarA(new PerfilFragment()));
        btnFotos.setOnClickListener(v -> navegarA(new FotosFragment()));
        btnServicios.setOnClickListener(v -> navegarA(new ServiciosFragment()));
        btnCitas.setOnClickListener(v -> navegarA(new CitasFragment()));

        marcarSeleccion(seleccionId);

        return view;
    }

    private void navegarA(Fragment destino) {
        ((MainActivity) requireActivity()).navegarA(destino);
    }

    public void marcarSeleccion(int botonId) {
        seleccionId = botonId;
        if (btnPerfil == null) {
            return;
        }
        for (Button boton : new Button[]{btnPerfil, btnFotos, btnServicios, btnCitas}) {
            boolean seleccionado = boton.getId() == botonId;
            boton.setAlpha(seleccionado ? 1f : 0.55f);
            boton.setSelected(seleccionado);
        }
    }
}
