package com.example.dogcare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

public class CitasFragment extends Fragment {

    private EditText etNombreMascota;
    private Spinner spServicio;
    private Button btnSeleccionarFecha;
    private Button btnSeleccionarHora;
    private Button btnAgendarCita;
    private TextView tvFechaSeleccionada;
    private TextView tvHoraSeleccionada;
    private TextView tvConfirmacion;

    private String fechaSeleccionada;
    private String horaSeleccionada;

    public CitasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas, container, false);

        etNombreMascota = view.findViewById(R.id.etNombreMascota);
        spServicio = view.findViewById(R.id.spServicio);
        btnSeleccionarFecha = view.findViewById(R.id.btnSeleccionarFecha);
        btnSeleccionarHora = view.findViewById(R.id.btnSeleccionarHora);
        btnAgendarCita = view.findViewById(R.id.btnAgendarCita);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
        tvHoraSeleccionada = view.findViewById(R.id.tvHoraSeleccionada);
        tvConfirmacion = view.findViewById(R.id.tvConfirmacion);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.lista_servicios, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServicio.setAdapter(adapter);

        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());
        btnSeleccionarHora.setOnClickListener(v -> mostrarSelectorHora());
        btnAgendarCita.setOnClickListener(v -> agendarCita());

        return view;
    }

    private void mostrarSelectorFecha() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    fechaSeleccionada = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void mostrarSelectorHora() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                (timePicker, hourOfDay, minute) -> {
                    horaSeleccionada = String.format(Locale.getDefault(),
                            "%02d:%02d", hourOfDay, minute);
                    tvHoraSeleccionada.setText("Hora: " + horaSeleccionada);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    private void agendarCita() {
        String nombreMascota = etNombreMascota.getText().toString().trim();
        String servicio = spServicio.getSelectedItem() != null
                ? spServicio.getSelectedItem().toString() : "";

        if (nombreMascota.isEmpty() || fechaSeleccionada == null || horaSeleccionada == null) {
            tvConfirmacion.setText("Completa el nombre de la mascota, la fecha y la hora antes de agendar.");
            return;
        }

        tvConfirmacion.setText("Cita agendada para " + nombreMascota + " — " + servicio
                + " el " + fechaSeleccionada + " a las " + horaSeleccionada + ".");
    }
}
