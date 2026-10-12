package com.example.dogcare;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/** Módulo Video: reproduce videos educativos con sus controles de reproducción. */
public class VideoFragment extends Fragment {

    private static final String ESTADO_INDICE = "indice";
    private static final String ESTADO_POSICION = "posicion";
    private static final String ESTADO_URI_TELEFONO = "uri_telefono";

    /** Videos incluidos en la app (res/raw). */
    private static final RecursoVideo[] VIDEOS = {
            new RecursoVideo("Baño de tu perro", R.raw.video_bano,
                    "Cómo bañarlo en casa: temperatura del agua, champú y secado."),
            new RecursoVideo("Cepillado del pelaje", R.raw.video_cepillado,
                    "Técnica y frecuencia de cepillado según el tipo de pelo."),
            new RecursoVideo("Primeros auxilios", R.raw.video_primeros_auxilios,
                    "Qué hacer ante heridas, golpes de calor y emergencias."),
            new RecursoVideo("Nutrición canina", R.raw.video_nutricion,
                    "Alimentos recomendados y alimentos prohibidos."),
            new RecursoVideo("Tips de cuidado diario", R.raw.video_tips,
                    "Rutinas de ejercicio, vacunas y controles veterinarios."),
    };

    private VideoView videoView;
    private TextView tvTituloVideo;
    private TextView tvDescripcionVideo;
    private TextView tvEstadoVideo;
    private ListView lvVideos;

    /** Índice del video de la lista, o -1 si se reproduce uno elegido del teléfono. */
    private int indiceActual = 0;
    private Uri uriTelefono;
    private int posicionGuardada = 0;

    private final ActivityResultLauncher<PickVisualMediaRequest> elegirVideo =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    return;
                }
                uriTelefono = uri;
                indiceActual = -1;
                lvVideos.clearChoices();
                lvVideos.requestLayout();
                cargarVideoActual(0, true);
            });

    public VideoFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        videoView = view.findViewById(R.id.videoView);
        tvTituloVideo = view.findViewById(R.id.tvTituloVideo);
        tvDescripcionVideo = view.findViewById(R.id.tvDescripcionVideo);
        tvEstadoVideo = view.findViewById(R.id.tvEstadoVideo);
        lvVideos = view.findViewById(R.id.lvVideos);

        // Barra de controles estándar de Android (play/pausa, adelantar, retroceder, progreso).
        MediaController controles = new MediaController(requireContext());
        controles.setAnchorView(view.findViewById(R.id.contenedorVideo));
        videoView.setMediaController(controles);

        videoView.setOnPreparedListener(mp -> mostrarEstado());
        videoView.setOnCompletionListener(mp -> tvEstadoVideo.setText("Estado: finalizado"));
        videoView.setOnErrorListener((mp, what, extra) -> {
            tvEstadoVideo.setText("No se pudo reproducir el video");
            return true;
        });

        lvVideos.setAdapter(new VideosAdapter());
        lvVideos.setOnItemClickListener((parent, v, position, id) -> {
            indiceActual = position;
            uriTelefono = null;
            cargarVideoActual(0, true);
        });

        view.findViewById(R.id.btnReproducir).setOnClickListener(v -> {
            videoView.start();
            tvEstadoVideo.setText("Estado: reproduciendo");
        });
        view.findViewById(R.id.btnPausar).setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
            }
            tvEstadoVideo.setText("Estado: en pausa (" + formatear(videoView.getCurrentPosition()) + ")");
        });
        view.findViewById(R.id.btnDetener).setOnClickListener(v -> {
            videoView.pause();
            videoView.seekTo(0);
            tvEstadoVideo.setText("Estado: detenido");
        });
        view.findViewById(R.id.btnElegirVideo).setOnClickListener(v ->
                elegirVideo.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                        .build()));

        if (savedInstanceState != null) {
            indiceActual = savedInstanceState.getInt(ESTADO_INDICE, 0);
            posicionGuardada = savedInstanceState.getInt(ESTADO_POSICION, 0);
            String uri = savedInstanceState.getString(ESTADO_URI_TELEFONO);
            uriTelefono = uri != null ? Uri.parse(uri) : null;
        }
        cargarVideoActual(posicionGuardada, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnVideo);
        videoView.seekTo(Math.max(posicionGuardada, 1));
    }

    @Override
    public void onPause() {
        super.onPause();
        posicionGuardada = videoView.getCurrentPosition();
        videoView.pause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_INDICE, indiceActual);
        outState.putInt(ESTADO_POSICION, posicionGuardada);
        if (uriTelefono != null) {
            outState.putString(ESTADO_URI_TELEFONO, uriTelefono.toString());
        }
    }

    private void cargarVideoActual(int posicion, boolean reproducir) {
        if (indiceActual >= 0) {
            RecursoVideo video = VIDEOS[indiceActual];
            videoView.setVideoURI(Uri.parse("android.resource://"
                    + requireContext().getPackageName() + "/" + video.recursoRaw));
            tvTituloVideo.setText(video.titulo);
            tvDescripcionVideo.setText(video.descripcion);
            lvVideos.setItemChecked(indiceActual, true);
        } else if (uriTelefono != null) {
            videoView.setVideoURI(uriTelefono);
            tvTituloVideo.setText("Video del teléfono");
            tvDescripcionVideo.setText("Video elegido desde la galería del dispositivo.");
        }
        posicionGuardada = posicion;
        videoView.seekTo(Math.max(posicion, 1));
        if (reproducir) {
            videoView.start();
            tvEstadoVideo.setText("Estado: reproduciendo");
            Toast.makeText(requireContext(), "Reproduciendo: " + tvTituloVideo.getText(),
                    Toast.LENGTH_SHORT).show();
        } else {
            tvEstadoVideo.setText("Estado: listo para reproducir");
        }
    }

    private void mostrarEstado() {
        if (!tvEstadoVideo.getText().toString().startsWith("Estado: reproduciendo")) {
            tvEstadoVideo.setText("Estado: listo para reproducir (duración "
                    + formatear(videoView.getDuration()) + ")");
        }
    }

    private static String formatear(int millis) {
        int segundos = Math.max(0, millis) / 1000;
        return String.format(java.util.Locale.getDefault(), "%d:%02d", segundos / 60, segundos % 60);
    }

    /** Datos de un video educativo de la lista. */
    private static final class RecursoVideo {
        final String titulo;
        final int recursoRaw;
        final String descripcion;

        RecursoVideo(String titulo, int recursoRaw, String descripcion) {
            this.titulo = titulo;
            this.recursoRaw = recursoRaw;
            this.descripcion = descripcion;
        }
    }

    private class VideosAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return VIDEOS.length;
        }

        @Override
        public Object getItem(int position) {
            return VIDEOS[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView != null ? convertView
                    : LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            RecursoVideo video = VIDEOS[position];
            ((TextView) item.findViewById(R.id.tvItemTitulo)).setText((position + 1) + ". " + video.titulo);
            ((TextView) item.findViewById(R.id.tvItemDuracion)).setText(video.descripcion);
            return item;
        }
    }
}
