package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/** Fragmento izquierdo: menú permanente que cambia el contenido del panel derecho. */
public class MenuFragment extends Fragment {

    private Button[] botones;

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

        Button btnPerfil = view.findViewById(R.id.btnPerfil);
        Button btnFotos = view.findViewById(R.id.btnFotos);
        Button btnVideo = view.findViewById(R.id.btnVideo);
        Button btnWeb = view.findViewById(R.id.btnWeb);
        Button btnBotones = view.findViewById(R.id.btnBotones);
        Button btnServicios = view.findViewById(R.id.btnServicios);
        Button btnCitas = view.findViewById(R.id.btnCitas);
        botones = new Button[]{btnPerfil, btnFotos, btnVideo, btnWeb, btnBotones,
                btnServicios, btnCitas};

        btnPerfil.setOnClickListener(v -> navegarA(new PerfilFragment()));
        btnFotos.setOnClickListener(v -> navegarA(new FotosFragment()));
        btnVideo.setOnClickListener(v -> navegarA(new VideoFragment()));
        btnWeb.setOnClickListener(v -> navegarA(new WebFragment()));
        btnBotones.setOnClickListener(v -> navegarA(new BotonesFragment()));
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
        if (botones == null) {
            return;
        }
        for (Button boton : botones) {
            boolean seleccionado = boton.getId() == botonId;
            boton.setAlpha(seleccionado ? 1f : 0.55f);
            boton.setSelected(seleccionado);
        }
    }
}
