package com.example.dogcare;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    public interface OnMenuOptionSelectedListener {
        void onPerfilSelected();
        void onFotosSelected();
        void onServiciosSelected();
        void onCitasSelected();
    }

    private Button btnPerfil;
    private Button btnFotos;
    private Button btnServicios;
    private Button btnCitas;

    private OnMenuOptionSelectedListener listener;

    public MenuFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMenuOptionSelectedListener) {
            listener = (OnMenuOptionSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " debe implementar OnMenuOptionSelectedListener");
        }
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

        btnPerfil.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPerfilSelected();
            }
        });

        btnFotos.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFotosSelected();
            }
        });

        btnServicios.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiciosSelected();
            }
        });

        btnCitas.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCitasSelected();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
