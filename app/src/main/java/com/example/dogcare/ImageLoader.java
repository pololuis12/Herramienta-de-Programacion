package com.example.dogcare;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.util.Size;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Carga miniaturas de imágenes en segundo plano y las guarda en caché.
 */
final class ImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };

    private ImageLoader() {
    }

    static void cargar(ImageView imageView, String uri, int tamanoPx) {
        String key = uri + "#" + tamanoPx;
        imageView.setTag(R.id.tag_image_key, key);

        Bitmap enCache = CACHE.get(key);
        if (enCache != null) {
            imageView.setImageBitmap(enCache);
            return;
        }

        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        Context appContext = imageView.getContext().getApplicationContext();
        EXECUTOR.execute(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = appContext.getContentResolver()
                        .loadThumbnail(Uri.parse(uri), new Size(tamanoPx, tamanoPx), null);
            } catch (Exception ignored) {
                // La imagen ya no existe o se perdió el permiso de lectura.
            }
            Bitmap resultado = bitmap;
            MAIN.post(() -> {
                if (!key.equals(imageView.getTag(R.id.tag_image_key))) {
                    return;
                }
                if (resultado != null) {
                    CACHE.put(key, resultado);
                    imageView.setImageBitmap(resultado);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            });
        });
    }
}
