package com.example.dogcare;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FotosFragment extends Fragment {

    private TextView tvFotosTitle;

    public FotosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fotos, container, false);

        tvFotosTitle = view.findViewById(R.id.tvFotosTitle);

        return view;
    }
}