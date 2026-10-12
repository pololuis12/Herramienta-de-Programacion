package com.example.dogcare;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    /** Id de la cita a mostrar al abrir la app desde una notificación. */
    public static final String EXTRA_ABRIR_CITA = "abrir_cita";

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

        // Solo se agregan los fragments la primera vez; al rotar se restauran solos.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.leftFragmentContainer, new MenuFragment())
                    .replace(R.id.rightFragmentContainer, new PerfilFragment())
                    .commit();
            abrirCitaDesdeNotificacion(getIntent());
        }

        Recordatorios.crearCanal(this);
        Recordatorios.reprogramarTodas(this);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        abrirCitaDesdeNotificacion(intent);
    }

    private void abrirCitaDesdeNotificacion(Intent intent) {
        String citaId = intent.getStringExtra(EXTRA_ABRIR_CITA);
        if (citaId == null) {
            return;
        }
        intent.removeExtra(EXTRA_ABRIR_CITA);
        navegarA(new CitasFragment());
        abrirEncima(DetalleCitaFragment.newInstance(citaId));
    }

    /**
     * Muestra el fragment indicado en el panel derecho. El Perfil es la pantalla raíz:
     * desde cualquier otra sección el botón "Atrás" regresa al Perfil.
     */
    public void navegarA(Fragment destino) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment actual = fm.findFragmentById(R.id.rightFragmentContainer);
        if (actual != null && actual.getClass() == destino.getClass()
                && destino.getArguments() == null) {
            return;
        }

        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (destino instanceof PerfilFragment) {
            return;
        }

        fm.beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.rightFragmentContainer, destino)
                .addToBackStack(null)
                .commit();
    }

    /** Abre una pantalla de detalle encima de la actual; "Atrás" regresa a la anterior. */
    public void abrirEncima(Fragment destino) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.rightFragmentContainer, destino)
                .addToBackStack(null)
                .commit();
    }

    /** Resalta en el menú el botón de la sección visible. */
    public void marcarSeccion(int botonMenuId) {
        Fragment menu = getSupportFragmentManager().findFragmentById(R.id.leftFragmentContainer);
        if (menu instanceof MenuFragment) {
            ((MenuFragment) menu).marcarSeleccion(botonMenuId);
        }
    }
}
