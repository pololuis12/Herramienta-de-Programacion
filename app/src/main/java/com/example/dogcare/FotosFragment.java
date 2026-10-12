package com.example.dogcare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class FotosFragment extends Fragment {

    private static final int MAX_FOTOS_POR_SELECCION = 10;

    private DogCareRepository repo;
    private final List<String> fotos = new ArrayList<>();
    private FotosAdapter adapter;

    private TextView tvContadorFotos;
    private TextView tvSinFotos;

    private final ActivityResultLauncher<PickVisualMediaRequest> seleccionarFotos =
            registerForActivityResult(
                    new ActivityResultContracts.PickMultipleVisualMedia(MAX_FOTOS_POR_SELECCION),
                    uris -> {
                        if (uris.isEmpty()) {
                            return;
                        }
                        for (Uri uri : uris) {
                            conservarPermiso(requireContext(), uri);
                            repo.agregarFoto(uri.toString());
                        }
                        actualizarFotos();
                        Toast.makeText(requireContext(),
                                uris.size() == 1 ? "Foto agregada" : uris.size() + " fotos agregadas",
                                Toast.LENGTH_SHORT).show();
                    });

    public FotosFragment() {
        // Constructor vacío requerido
    }

    /** Mantiene el permiso de lectura de la foto aunque se reinicie la app. */
    static void conservarPermiso(Context context, Uri uri) {
        try {
            context.getContentResolver()
                    .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Algunos proveedores no permiten permisos persistentes.
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fotos, container, false);
        repo = new DogCareRepository(requireContext());

        tvContadorFotos = view.findViewById(R.id.tvContadorFotos);
        tvSinFotos = view.findViewById(R.id.tvSinFotos);
        GridView gvFotos = view.findViewById(R.id.gvFotos);

        adapter = new FotosAdapter();
        gvFotos.setAdapter(adapter);
        gvFotos.setOnItemClickListener((parent, v, position, id) ->
                mostrarFoto(position));

        view.findViewById(R.id.btnAgregarFotos).setOnClickListener(v ->
                seleccionarFotos.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        view.findViewById(R.id.btnVolverPerfil).setOnClickListener(v ->
                ((MainActivity) requireActivity()).navegarA(new PerfilFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnFotos);
        actualizarFotos();
    }

    private void actualizarFotos() {
        fotos.clear();
        fotos.addAll(repo.getFotos());
        adapter.notifyDataSetChanged();

        tvContadorFotos.setText("Fotos de " + repo.getNombre() + ": " + fotos.size());
        tvSinFotos.setVisibility(fotos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Muestra la foto ampliada con su descripción; permite pasar a la anterior o siguiente. */
    private void mostrarFoto(int posicionInicial) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_foto, null);
        TextView tvPosicion = vista.findViewById(R.id.tvPosicionFoto);
        ImageView ivFoto = vista.findViewById(R.id.ivFotoAmpliada);
        TextView tvDescripcion = vista.findViewById(R.id.tvDescripcionFoto);
        Button btnAnterior = vista.findViewById(R.id.btnFotoAnterior);
        Button btnSiguiente = vista.findViewById(R.id.btnFotoSiguiente);
        Button btnUsarPerfil = vista.findViewById(R.id.btnUsarPerfil);
        tvDescripcion.setMovementMethod(new ScrollingMovementMethod());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(vista)
                .setPositiveButton("Editar descripción", null)
                .setNeutralButton("Cerrar", null)
                .create();

        int[] posicion = {posicionInicial};
        Runnable mostrar = () -> {
            String uri = fotos.get(posicion[0]);
            tvPosicion.setText("Foto " + (posicion[0] + 1) + " de " + fotos.size());
            ImageLoader.cargar(ivFoto, uri, 1024);
            String descripcion = repo.getDescripcionFoto(uri);
            tvDescripcion.setText(descripcion.isEmpty()
                    ? "Sin descripción. Toca «Editar descripción» para agregar una." : descripcion);
            tvDescripcion.scrollTo(0, 0);
            btnAnterior.setEnabled(posicion[0] > 0);
            btnSiguiente.setEnabled(posicion[0] < fotos.size() - 1);
            boolean esFotoPerfil = uri.equals(repo.getFotoPerfil());
            btnUsarPerfil.setEnabled(!esFotoPerfil);
            btnUsarPerfil.setText(esFotoPerfil ? "Foto de perfil" : "Usar como perfil");
        };

        btnAnterior.setOnClickListener(v -> {
            posicion[0]--;
            mostrar.run();
        });
        btnSiguiente.setOnClickListener(v -> {
            posicion[0]++;
            mostrar.run();
        });
        btnUsarPerfil.setOnClickListener(v -> {
            repo.setFotoPerfil(fotos.get(posicion[0]));
            actualizarFotos();
            mostrar.run();
            Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        });
        vista.findViewById(R.id.btnEliminarFoto).setOnClickListener(v -> {
            dialog.dismiss();
            confirmarEliminar(fotos.get(posicion[0]));
        });
        // Se sobreescribe el botón para que el diálogo siga abierto tras editar.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> editarDescripcion(fotos.get(posicion[0]), mostrar)));

        mostrar.run();
        dialog.show();
    }

    private void editarDescripcion(String uri, Runnable alGuardar) {
        EditText etDescripcion = new EditText(requireContext());
        etDescripcion.setHint("Ej.: Lucas después del baño en la clínica");
        etDescripcion.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etDescripcion.setMaxLines(5);
        etDescripcion.setText(repo.getDescripcionFoto(uri));
        FrameLayout contenedor = new FrameLayout(requireContext());
        int margen = (int) (20 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(margen, margen / 2, margen, 0);
        contenedor.addView(etDescripcion);

        new AlertDialog.Builder(requireContext())
                .setTitle("Descripción de la foto")
                .setView(contenedor)
                .setPositiveButton("Guardar", (d, w) -> {
                    repo.setDescripcionFoto(uri, etDescripcion.getText().toString().trim());
                    alGuardar.run();
                    Toast.makeText(requireContext(), "Descripción guardada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar(String uri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar foto")
                .setMessage("¿Quitar esta foto de la galería de DogCare? La imagen original no se borra del teléfono.")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repo.eliminarFoto(uri);
                    actualizarFotos();
                    Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private class FotosAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fotos.size();
        }

        @Override
        public Object getItem(int position) {
            return fotos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return fotos.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView != null ? convertView
                    : LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foto, parent, false);
            String uri = fotos.get(position);
            ImageLoader.cargar(item.findViewById(R.id.ivFoto), uri, 300);
            item.findViewById(R.id.tvEtiquetaPerfil).setVisibility(
                    uri.equals(repo.getFotoPerfil()) ? View.VISIBLE : View.GONE);
            return item;
        }
    }
}
