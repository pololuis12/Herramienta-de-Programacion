package com.example.dogcare;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Módulo Botones: ejemplo de Button, ImageButton, ToggleButton, Switch, CheckBox y
 * RadioButton. Cada evento se muestra en pantalla y en un Toast.
 */
public class BotonesFragment extends Fragment {

    static final String TELEFONO_CLINICA = "6015550123";

    private static final int PRECIO_GENERAL = 45000;
    private static final int PRECIO_URGENCIA = 80000;
    private static final int PRECIO_BANO = 35000;
    private static final int PRECIO_CORTE = 30000;
    private static final int PRECIO_VACUNACION = 40000;
    private static final int PRECIO_DOMICILIO = 15000;

    private DogCareRepository repo;

    private RadioGroup rgTipoConsulta;
    private CheckBox cbBano;
    private CheckBox cbCorte;
    private CheckBox cbVacunacion;
    private MaterialSwitch swDomicilio;
    private ToggleButton tbRecordatorios;
    private TextView tvTotal;
    private TextView tvResultado;

    public BotonesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_botones, container, false);
        repo = new DogCareRepository(requireContext());

        rgTipoConsulta = view.findViewById(R.id.rgTipoConsulta);
        cbBano = view.findViewById(R.id.cbBano);
        cbCorte = view.findViewById(R.id.cbCorte);
        cbVacunacion = view.findViewById(R.id.cbVacunacion);
        swDomicilio = view.findViewById(R.id.swDomicilio);
        tbRecordatorios = view.findViewById(R.id.tbRecordatorios);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvResultado = view.findViewById(R.id.tvResultado);

        // RadioButton: solo un tipo de consulta a la vez.
        rgTipoConsulta.setOnCheckedChangeListener((group, checkedId) -> evento(
                "RadioButton", "Tipo de consulta: "
                        + (checkedId == R.id.rbUrgencia ? "Urgencia" : "Consulta general")));

        // CheckBox: servicios adicionales.
        CompoundButton.OnCheckedChangeListener alMarcar = (boton, marcado) -> evento(
                "CheckBox", nombreServicio(boton) + (marcado ? " agregado" : " quitado"));
        cbBano.setOnCheckedChangeListener(alMarcar);
        cbCorte.setOnCheckedChangeListener(alMarcar);
        cbVacunacion.setOnCheckedChangeListener(alMarcar);

        // Switch: servicio a domicilio.
        swDomicilio.setOnCheckedChangeListener((boton, activo) -> evento(
                "Switch", activo ? "Servicio a domicilio activado" : "Atención en la clínica"));

        // ToggleButton: activa o desactiva los recordatorios reales de las citas.
        tbRecordatorios.setChecked(repo.getRecordatoriosActivos());
        tbRecordatorios.setOnCheckedChangeListener((boton, activo) -> {
            repo.setRecordatoriosActivos(activo);
            evento("ToggleButton", activo ? "Recordatorios de citas activados"
                    : "Recordatorios de citas desactivados");
        });

        // ImageButton: abre el marcador con el teléfono de la clínica.
        view.findViewById(R.id.ibLlamar).setOnClickListener(v -> {
            evento("ImageButton", "Llamando a la clínica: " + TELEFONO_CLINICA);
            try {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + TELEFONO_CLINICA)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "No hay una app de teléfono", Toast.LENGTH_SHORT).show();
            }
        });

        // Button: agenda la cita con el servicio elegido.
        view.findViewById(R.id.btnSolicitar).setOnClickListener(v -> {
            String servicio = servicioPrincipal();
            evento("Button", "Agendando " + servicio + " por " + formatearPrecio(calcularTotal()));
            ((MainActivity) requireActivity()).navegarA(CitasFragment.newInstance(servicio));
        });

        // Button: vuelve todos los controles a su estado inicial.
        view.findViewById(R.id.btnLimpiar).setOnClickListener(v -> {
            rgTipoConsulta.check(R.id.rbGeneral);
            cbBano.setChecked(false);
            cbCorte.setChecked(false);
            cbVacunacion.setChecked(false);
            swDomicilio.setChecked(false);
            evento("Button", "Selección reiniciada");
        });

        actualizarTotal();
        tvResultado.setText("Toca cualquier control para ver su evento.");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnBotones);
    }

    /** Muestra el evento generado por un control en pantalla y en un Toast. */
    private void evento(String control, String mensaje) {
        actualizarTotal();
        tvResultado.setText("Evento (" + control + "): " + mensaje);
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private void actualizarTotal() {
        List<String> partes = new ArrayList<>();
        partes.add(rgTipoConsulta.getCheckedRadioButtonId() == R.id.rbUrgencia
                ? "Urgencia" : "Consulta general");
        if (cbBano.isChecked()) partes.add("Baño");
        if (cbCorte.isChecked()) partes.add("Corte");
        if (cbVacunacion.isChecked()) partes.add("Vacunación");
        if (swDomicilio.isChecked()) partes.add("Domicilio");
        tvTotal.setText("Selección: " + String.join(" + ", partes)
                + "\nTotal estimado: " + formatearPrecio(calcularTotal()));
    }

    private int calcularTotal() {
        int total = rgTipoConsulta.getCheckedRadioButtonId() == R.id.rbUrgencia
                ? PRECIO_URGENCIA : PRECIO_GENERAL;
        if (cbBano.isChecked()) total += PRECIO_BANO;
        if (cbCorte.isChecked()) total += PRECIO_CORTE;
        if (cbVacunacion.isChecked()) total += PRECIO_VACUNACION;
        if (swDomicilio.isChecked()) total += PRECIO_DOMICILIO;
        return total;
    }

    /** Servicio del catálogo con el que se precarga el formulario de citas. */
    private String servicioPrincipal() {
        if (cbVacunacion.isChecked()) {
            return "Vacunación";
        }
        if (cbBano.isChecked() || cbCorte.isChecked()) {
            return "Baño y peluquería";
        }
        return "Consulta general";
    }

    private String nombreServicio(CompoundButton boton) {
        if (boton == cbBano) return "Baño";
        if (boton == cbCorte) return "Corte de pelo";
        return "Vacunación";
    }

    private static String formatearPrecio(int valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "$%,d", valor);
    }
}
