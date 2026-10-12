package com.example.dogcare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
                mostrarOpcionesFoto(fotos.get(position)));

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

    private void mostrarOpcionesFoto(String uri) {
        ImageView vistaPrevia = new ImageView(requireContext());
        vistaPrevia.setAdjustViewBounds(true);
        vistaPrevia.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        vistaPrevia.setPadding(padding, padding, padding, 0);
        ImageLoader.cargar(vistaPrevia, uri, 1024);

        boolean esFotoPerfil = uri.equals(repo.getFotoPerfil());

        new AlertDialog.Builder(requireContext())
                .setView(vistaPrevia)
                .setPositiveButton(esFotoPerfil ? "Es la foto de perfil" : "Usar como perfil",
                        (d, w) -> {
                            if (!esFotoPerfil) {
                                repo.setFotoPerfil(uri);
                                actualizarFotos();
                                Toast.makeText(requireContext(), "Foto de perfil actualizada",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("Eliminar", (d, w) -> confirmarEliminar(uri))
                .setNeutralButton("Cerrar", null)
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
