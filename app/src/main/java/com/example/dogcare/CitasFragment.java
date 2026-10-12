package com.example.dogcare;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CitasFragment extends Fragment {

    private static final String ARG_SERVICIO = "servicio";
    private static final String STATE_ANIO = "anio";
    private static final String STATE_MES = "mes";
    private static final String STATE_DIA = "dia";
    private static final String STATE_HORA = "hora";
    private static final String STATE_MINUTO = "minuto";

    private DogCareRepository repo;

    private EditText etNombreMascota;
    private Spinner spServicio;
    private Button btnSeleccionarFecha;
    private Button btnSeleccionarHora;
    private Button btnAgendarCita;
    private TextView tvFechaSeleccionada;
    private TextView tvHoraSeleccionada;
    private TextView tvConfirmacion;
    private TextView tvSinCitas;
    private TextView tvTituloHistorial;
    private LinearLayout llCitas;
    private LinearLayout llHistorialCitas;
    private MaterialSwitch swRecordatorios;
    private TextView tvEstadoRecordatorios;

    /** El usuario negó el permiso y pidió no volver a preguntar: solo queda ir a Ajustes. */
    private boolean permisoBloqueado;

    private final ActivityResultLauncher<String> solicitarPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                permisoBloqueado = !concedido && !shouldShowRequestPermissionRationale(
                        Manifest.permission.POST_NOTIFICATIONS);
                if (concedido) {
                    Recordatorios.reprogramarTodas(requireContext());
                }
                mostrarEstadoRecordatorios();
            });

    // -1 indica que todavía no se ha seleccionado.
    private int anio = -1, mes = -1, dia = -1;
    private int hora = -1, minuto = -1;

    public CitasFragment() {
        // Constructor vacío requerido
    }

    /** Abre la pantalla de citas con un servicio ya seleccionado. */
    public static CitasFragment newInstance(String servicio) {
        CitasFragment fragment = new CitasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVICIO, servicio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas, container, false);
        repo = new DogCareRepository(requireContext());

        etNombreMascota = view.findViewById(R.id.etNombreMascota);
        spServicio = view.findViewById(R.id.spServicio);
        btnSeleccionarFecha = view.findViewById(R.id.btnSeleccionarFecha);
        btnSeleccionarHora = view.findViewById(R.id.btnSeleccionarHora);
        btnAgendarCita = view.findViewById(R.id.btnAgendarCita);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
        tvHoraSeleccionada = view.findViewById(R.id.tvHoraSeleccionada);
        tvConfirmacion = view.findViewById(R.id.tvConfirmacion);
        tvSinCitas = view.findViewById(R.id.tvSinCitas);
        llCitas = view.findViewById(R.id.llCitas);
        tvTituloHistorial = view.findViewById(R.id.tvTituloHistorial);
        llHistorialCitas = view.findViewById(R.id.llHistorialCitas);
        swRecordatorios = view.findViewById(R.id.swRecordatorios);
        tvEstadoRecordatorios = view.findViewById(R.id.tvEstadoRecordatorios);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.lista_servicios, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spServicio.setAdapter(adapter);

        if (savedInstanceState == null) {
            etNombreMascota.setText(repo.getNombre());
            String servicio = getArguments() != null ? getArguments().getString(ARG_SERVICIO) : null;
            if (servicio != null) {
                int posicion = adapter.getPosition(servicio);
                if (posicion >= 0) {
                    spServicio.setSelection(posicion);
                }
            }
        } else {
            anio = savedInstanceState.getInt(STATE_ANIO, -1);
            mes = savedInstanceState.getInt(STATE_MES, -1);
            dia = savedInstanceState.getInt(STATE_DIA, -1);
            hora = savedInstanceState.getInt(STATE_HORA, -1);
            minuto = savedInstanceState.getInt(STATE_MINUTO, -1);
        }
        mostrarFechaHora();

        btnSeleccionarFecha.setOnClickListener(v -> mostrarSelectorFecha());
        btnSeleccionarHora.setOnClickListener(v -> mostrarSelectorHora());
        btnAgendarCita.setOnClickListener(v -> agendarCita());
        view.findViewById(R.id.btnVerHistoriaClinica).setOnClickListener(v ->
                ((MainActivity) requireActivity()).abrirEncima(new HistoriaClinicaFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnCitas);
        mostrarCitas();
        mostrarEstadoRecordatorios();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ANIO, anio);
        outState.putInt(STATE_MES, mes);
        outState.putInt(STATE_DIA, dia);
        outState.putInt(STATE_HORA, hora);
        outState.putInt(STATE_MINUTO, minuto);
    }

    private void mostrarSelectorFecha() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    anio = year;
                    mes = month;
                    dia = dayOfMonth;
                    mostrarFechaHora();
                },
                anio >= 0 ? anio : calendar.get(Calendar.YEAR),
                mes >= 0 ? mes : calendar.get(Calendar.MONTH),
                dia >= 0 ? dia : calendar.get(Calendar.DAY_OF_MONTH));
        // No se permiten fechas pasadas.
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis() - 1000);
        dialog.show();
    }

    private void mostrarSelectorHora() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                (timePicker, hourOfDay, minute) -> {
                    hora = hourOfDay;
                    minuto = minute;
                    mostrarFechaHora();
                },
                hora >= 0 ? hora : calendar.get(Calendar.HOUR_OF_DAY),
                minuto >= 0 ? minuto : calendar.get(Calendar.MINUTE),
                true);
        dialog.show();
    }

    private void mostrarFechaHora() {
        tvFechaSeleccionada.setText(anio >= 0
                ? String.format(Locale.getDefault(), "Fecha: %02d/%02d/%d", dia, mes + 1, anio)
                : "Fecha: sin seleccionar");
        tvHoraSeleccionada.setText(hora >= 0
                ? String.format(Locale.getDefault(), "Hora: %02d:%02d", hora, minuto)
                : "Hora: sin seleccionar");
    }

    private void agendarCita() {
        String nombreMascota = etNombreMascota.getText().toString().trim();
        String servicio = spServicio.getSelectedItem() != null
                ? spServicio.getSelectedItem().toString() : "";

        if (nombreMascota.isEmpty() || anio < 0 || hora < 0) {
            tvConfirmacion.setText("Completa el nombre de la mascota, la fecha y la hora antes de agendar.");
            return;
        }

        Calendar fechaHora = Calendar.getInstance();
        fechaHora.set(anio, mes, dia, hora, minuto, 0);
        fechaHora.set(Calendar.MILLISECOND, 0);
        if (fechaHora.getTimeInMillis() <= System.currentTimeMillis()) {
            tvConfirmacion.setText("La fecha y hora de la cita deben ser posteriores al momento actual.");
            return;
        }

        Cita conflicto = repo.buscarConflicto(fechaHora.getTimeInMillis(), null);
        if (conflicto != null) {
            tvConfirmacion.setText("Ese horario se cruza con otra cita (" + conflicto.servicio + ", "
                    + conflicto.getFechaHoraTexto() + "). Deja al menos "
                    + DogCareRepository.MINUTOS_ENTRE_CITAS + " minutos entre citas.");
            return;
        }

        Cita cita = new Cita(nombreMascota, servicio, fechaHora.getTimeInMillis());
        repo.agregarCita(cita);

        tvConfirmacion.setText("Cita reservada para " + nombreMascota + " — " + servicio
                + " el " + cita.getFechaHoraTexto() + ". Código " + cita.codigo
                + ".\nEstado: pendiente de confirmar. Toca «Ver» para confirmarla."
                + textoRecordatorios(cita));

        if (repo.getRecordatoriosActivos() && !Recordatorios.tienePermiso(requireContext())
                && !permisoBloqueado) {
            solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        anio = mes = dia = hora = minuto = -1;
        mostrarFechaHora();
        mostrarCitas();
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

    private void mostrarEstadoRecordatorios() {
        boolean activos = repo.getRecordatoriosActivos();
        swRecordatorios.setOnCheckedChangeListener(null);
        swRecordatorios.setChecked(activos);
        swRecordatorios.setOnCheckedChangeListener((boton, marcado) -> cambiarRecordatorios(marcado));

        tvEstadoRecordatorios.setOnClickListener(null);
        if (!activos) {
            tvEstadoRecordatorios.setText("Desactivados: no recibirás avisos de tus citas.");
        } else if (!Recordatorios.tienePermiso(requireContext())) {
            tvEstadoRecordatorios.setText(permisoBloqueado
                    ? "Las notificaciones están bloqueadas. Toca aquí para abrir los ajustes."
                    : "Falta el permiso de notificaciones. Toca aquí para concederlo.");
            tvEstadoRecordatorios.setOnClickListener(v -> pedirPermiso());
        } else if (!Recordatorios.puedeUsarAlarmasExactas(requireContext())) {
            tvEstadoRecordatorios.setText("Recibirás un aviso 24 h y 1 h antes de cada cita, "
                    + "con hasta 10 min de diferencia. Toca aquí para permitir avisos puntuales.");
            tvEstadoRecordatorios.setOnClickListener(v -> startActivity(
                    new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + requireContext().getPackageName()))));
        } else {
            tvEstadoRecordatorios.setText(
                    "Recibirás un aviso 24 h y 1 h antes de cada cita pendiente o confirmada.");
        }
    }

    private void cambiarRecordatorios(boolean activar) {
        repo.setRecordatoriosActivos(activar);
        if (activar && !Recordatorios.tienePermiso(requireContext())) {
            pedirPermiso();
        }
        mostrarEstadoRecordatorios();
    }

    private void pedirPermiso() {
        if (permisoBloqueado) {
            startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName()));
        } else {
            solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void mostrarCitas() {
        llCitas.removeAllViews();
        llHistorialCitas.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<Cita> citas = repo.getCitas();
        // El historial se muestra de la más reciente a la más antigua.
        for (int i = citas.size() - 1; i >= 0; i--) {
            Cita cita = citas.get(i);
            if (!cita.estado.esActiva()) {
                llHistorialCitas.addView(crearFila(inflater, cita, llHistorialCitas));
            }
        }
        for (Cita cita : citas) {
            if (cita.estado.esActiva()) {
                llCitas.addView(crearFila(inflater, cita, llCitas));
            }
        }

        tvSinCitas.setVisibility(llCitas.getChildCount() == 0 ? View.VISIBLE : View.GONE);
        int visibilidadHistorial = llHistorialCitas.getChildCount() == 0 ? View.GONE : View.VISIBLE;
        tvTituloHistorial.setVisibility(visibilidadHistorial);
    }

    private View crearFila(LayoutInflater inflater, Cita cita, ViewGroup padre) {
        View fila = inflater.inflate(R.layout.item_cita, padre, false);
        TextView tvEstado = fila.findViewById(R.id.tvCitaEstado);
        tvEstado.setText(cita.estado.etiqueta);
        tvEstado.setBackgroundColor(ContextCompat.getColor(requireContext(), cita.estado.colorRes));
        ((TextView) fila.findViewById(R.id.tvCitaInfo)).setText(cita.getResumen());

        View.OnClickListener abrir = v -> ((MainActivity) requireActivity())
                .abrirEncima(DetalleCitaFragment.newInstance(cita.id));
        fila.setOnClickListener(abrir);
        fila.findViewById(R.id.btnVerCita).setOnClickListener(abrir);
        return fila;
    }
}
