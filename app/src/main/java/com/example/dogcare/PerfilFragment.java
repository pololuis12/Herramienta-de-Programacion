package com.example.dogcare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private DogCareRepository repo;

    private ImageView ivFotoPerfil;
    private TextView tvPetName;
    private TextView tvPetBreed;
    private TextView tvPetEdad;
    private TextView tvPetPeso;
    private TextView tvDueno;
    private TextView tvTelefono;
    private TextView tvProximaCita;
    private TextView tvTituloDescripcion;
    private TextView tvDescripcion;
    private Button btnVerFotos;

    private final ActivityResultLauncher<PickVisualMediaRequest> seleccionarFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    return;
                }
                FotosFragment.conservarPermiso(requireContext(), uri);
                repo.agregarFoto(uri.toString());
                repo.setFotoPerfil(uri.toString());
                mostrarDatos();
                Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
            });

    public PerfilFragment() {
        // Constructor vacío requerido
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        repo = new DogCareRepository(requireContext());

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvPetBreed = view.findViewById(R.id.tvPetBreed);
        tvPetEdad = view.findViewById(R.id.tvPetEdad);
        tvPetPeso = view.findViewById(R.id.tvPetPeso);
        tvDueno = view.findViewById(R.id.tvDueno);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvProximaCita = view.findViewById(R.id.tvProximaCita);
        btnVerFotos = view.findViewById(R.id.btnVerFotos);
        tvTituloDescripcion = view.findViewById(R.id.tvTituloDescripcion);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);

        // El texto se desplaza dentro de su caja sin mover toda la pantalla.
        tvDescripcion.setMovementMethod(new ScrollingMovementMethod());
        tvDescripcion.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(v.canScrollVertically(1)
                    || v.canScrollVertically(-1));
            return false;
        });

        ivFotoPerfil.setOnClickListener(v -> abrirSelectorFoto());
        view.findViewById(R.id.btnCambiarFoto).setOnClickListener(v -> abrirSelectorFoto());
        view.findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> mostrarDialogoEditar());
        btnVerFotos.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navegarA(new FotosFragment()));
        view.findViewById(R.id.btnHistoriaClinica).setOnClickListener(v ->
                ((MainActivity) requireActivity()).abrirEncima(new HistoriaClinicaFragment()));
        view.findViewById(R.id.btnAgendarDesdePerfil).setOnClickListener(v ->
                ((MainActivity) requireActivity()).navegarA(new CitasFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnPerfil);
        mostrarDatos();
    }

    private void mostrarDatos() {
        tvPetName.setText(repo.getNombre());
        tvPetBreed.setText(repo.getRaza());
        tvPetEdad.setText("Edad: " + valorOVacio(repo.getEdad()));
        tvPetPeso.setText("Peso: " + valorOVacio(repo.getPeso()));
        tvDueno.setText("Dueño: " + valorOVacio(repo.getDueno()));
        tvTelefono.setText("Teléfono: " + valorOVacio(repo.getTelefono()));
        tvTituloDescripcion.setText("Sobre " + repo.getNombre());
        tvDescripcion.setText(valorOVacio(repo.getDescripcion()));

        Cita proxima = repo.getProximaCita();
        tvProximaCita.setText(proxima != null
                ? "Próxima cita (" + proxima.estado.etiqueta.toLowerCase() + "): "
                        + proxima.servicio + ", " + proxima.getFechaHoraTexto()
                : "Sin citas próximas");

        int totalFotos = repo.getFotos().size();
        btnVerFotos.setText("Ver fotos (" + totalFotos + ")");

        String fotoPerfil = repo.getFotoPerfil();
        if (fotoPerfil != null) {
            ImageLoader.cargar(ivFotoPerfil, fotoPerfil, 400);
        } else {
            ivFotoPerfil.setTag(R.id.tag_image_key, null);
            ivFotoPerfil.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private String valorOVacio(String valor) {
        return valor == null || valor.isEmpty() ? "—" : valor;
    }

    private void abrirSelectorFoto() {
        seleccionarFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void mostrarDialogoEditar() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);
        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etRaza = dialogView.findViewById(R.id.etRaza);
        EditText etEdad = dialogView.findViewById(R.id.etEdad);
        EditText etPeso = dialogView.findViewById(R.id.etPeso);
        EditText etDueno = dialogView.findViewById(R.id.etDueno);
        EditText etTelefono = dialogView.findViewById(R.id.etTelefono);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);

        etNombre.setText(repo.getNombre());
        etRaza.setText(repo.getRaza());
        etEdad.setText(repo.getEdad());
        etPeso.setText(repo.getPeso());
        etDueno.setText(repo.getDueno());
        etTelefono.setText(repo.getTelefono());
        etDescripcion.setText(repo.getDescripcion());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        // Se sobreescribe el botón para que el diálogo no se cierre si hay errores.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        etNombre.setError("El nombre es obligatorio");
                        return;
                    }
                    repo.guardarPerfil(
                            nombre,
                            etRaza.getText().toString().trim(),
                            etEdad.getText().toString().trim(),
                            etPeso.getText().toString().trim(),
                            etDueno.getText().toString().trim(),
                            etTelefono.getText().toString().trim());
                    repo.setDescripcion(etDescripcion.getText().toString().trim());
                    mostrarDatos();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Perfil guardado", Toast.LENGTH_SHORT).show();
                }));
        dialog.show();
    }
}
