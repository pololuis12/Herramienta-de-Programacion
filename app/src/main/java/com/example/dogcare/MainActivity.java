package com.example.dogcare;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements MenuFragment.OnMenuOptionSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.leftFragmentContainer, new MenuFragment())
                    .replace(R.id.rightFragmentContainer, new PerfilFragment())
                    .commit();
        }
    }

    @Override
    public void onPerfilSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new PerfilFragment())
                .commit();
    }

    @Override
    public void onFotosSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new FotosFragment())
                .commit();
    }

    @Override
    public void onServiciosSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new ServiciosFragment())
                .commit();
    }

    @Override
    public void onCitasSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rightFragmentContainer, new CitasFragment())
                .commit();
    }
}
