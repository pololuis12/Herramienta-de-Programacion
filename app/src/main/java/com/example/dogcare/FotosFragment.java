package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FotosFragment extends Fragment implements FotoAdapter.OnFotoClickListener {

    private TextView tvFotosTitle;
    private RecyclerView rvFotos;
    private TextView tvFotoDetalleLabel;
    private ImageView ivFotoPreview;
    private TextView tvFotoDetalle;

    private FotoAdapter fotoAdapter;
    private List<FotoItem> listaFotos;

    public FotosFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fotos, container, false);

        tvFotosTitle = view.findViewById(R.id.tvFotosTitle);
        rvFotos = view.findViewById(R.id.rvFotos);
        tvFotoDetalleLabel = view.findViewById(R.id.tvFotoDetalleLabel);
        ivFotoPreview = view.findViewById(R.id.ivFotoPreview);
        tvFotoDetalle = view.findViewById(R.id.tvFotoDetalle);

        inicializarListaFotos();

        rvFotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        fotoAdapter = new FotoAdapter(listaFotos, this);
        rvFotos.setAdapter(fotoAdapter);

        // Mostrar la primera foto por defecto en el detalle
        if (!listaFotos.isEmpty()) {
            mostrarDetalleFoto(listaFotos.get(0));
        }

        return view;
    }

    private void inicializarListaFotos() {
        listaFotos = new ArrayList<>();
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo1,
                "Juego en el parque",
                "10/05/2024",
                "Lucas disfrutando de una tarde soleada jugando a la pelota en el parque."
        ));
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo2,
                "Día de baño",
                "18/05/2024",
                "Sesión de baño y peluquería en la veterinaria DogCare. ¡Quedó impecable!"
        ));
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo3,
                "Control veterinario",
                "02/06/2024",
                "Chequeo general y vacunación de rutina. Lucas se portó excelente."
        ));
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo4,
                "Hora de la siesta",
                "15/06/2024",
                "Lucas descansando plácidamente en su cama después de una larga caminata."
        ));
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo5,
                "Paseo por el campo",
                "28/06/2024",
                "Excursión al campo rodeado de naturaleza y aire fresco."
        ));
        listaFotos.add(new FotoItem(
                R.drawable.ic_dog_photo6,
                "Cumpleaños #3",
                "05/07/2024",
                "Celebrando sus 3 años con un pastel especial para perros."
        ));
    }

    @Override
    public void onFotoClick(FotoItem fotoItem) {
        mostrarDetalleFoto(fotoItem);
    }

    private void mostrarDetalleFoto(FotoItem foto) {
        if (foto != null) {
            ivFotoPreview.setImageResource(foto.getImageResId());
            tvFotoDetalle.setText(foto.getTitulo() + " (" + foto.getFecha() + "):\n" + foto.getDescripcion());
        }
    }
}
