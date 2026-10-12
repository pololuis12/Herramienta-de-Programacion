package com.example.dogcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private TextView tvPerfilTitle;
    private ImageView ivPetPhoto;
    private TextView tvPetName;
    private TextView tvPetBreed;
    private TextView tvPetAge;
    private TextView tvPetWeight;
    private TextView tvPetGender;
    private TextView tvPetMicrochip;
    private TextView tvPetVaccines;
    private TextView tvOwnerName;
    private TextView tvOwnerPhone;
    private TextView tvOwnerEmail;

    public PerfilFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvPerfilTitle = view.findViewById(R.id.tvPerfilTitle);
        ivPetPhoto = view.findViewById(R.id.ivPetPhoto);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvPetBreed = view.findViewById(R.id.tvPetBreed);
        tvPetAge = view.findViewById(R.id.tvPetAge);
        tvPetWeight = view.findViewById(R.id.tvPetWeight);
        tvPetGender = view.findViewById(R.id.tvPetGender);
        tvPetMicrochip = view.findViewById(R.id.tvPetMicrochip);
        tvPetVaccines = view.findViewById(R.id.tvPetVaccines);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvOwnerPhone = view.findViewById(R.id.tvOwnerPhone);
        tvOwnerEmail = view.findViewById(R.id.tvOwnerEmail);

        // Se asignan o reafirman datos de la mascota dinámicamente
        tvPetName.setText("Lucas");
        tvPetBreed.setText("Golden Retriever");
        tvPetAge.setText("Edad: 3 años");
        tvPetWeight.setText("Peso: 28 kg");
        tvPetGender.setText("Género: Macho (Esterilizado)");
        tvPetMicrochip.setText("Microchip: ID 985141002345678");
        tvPetVaccines.setText("Vacunación: Al día (Séxtuple, Rabia, Tos de perreras)");

        tvOwnerName.setText("Propietario: Carlos Gómez");
        tvOwnerPhone.setText("Teléfono: +57 300 123 4567");
        tvOwnerEmail.setText("Email: carlos.gomez@email.com");

        return view;
    }
}
