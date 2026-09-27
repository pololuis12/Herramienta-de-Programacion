package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    private Button btnPerfil;
    private Button btnFotos;
    private Button btnServicios;
    private Button btnCitas;

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

        btnPerfil.setOnClickListener(v -> mostrarPerfil());
        btnFotos.setOnClickListener(v -> mostrarFotos());
        btnServicios.setOnClickListener(v -> mostrarServicios());
        btnCitas.setOnClickListener(v -> mostrarCitas());

        return view;
    }

    private void mostrarPerfil() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new PerfilFragment())
                .commit();
    }

    private void mostrarFotos() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new FotosFragment())
                .commit();
    }

    private void mostrarServicios() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new ServiciosFragment())
                .commit();
    }

    private void mostrarCitas() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new CitasFragment())
                .commit();
    }
}