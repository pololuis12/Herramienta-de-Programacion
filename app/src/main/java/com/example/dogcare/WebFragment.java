package com.example.dogcare;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Módulo Web: carga en un WebView la página cuya URL se escribe en la caja de texto. */
public class WebFragment extends Fragment {

    /** Página de información canina que se abre por defecto. */
    static final String URL_INICIO = "https://es.m.wikipedia.org/wiki/Canis_familiaris";

    private EditText etUrl;
    private WebView webView;
    private ProgressBar pbCarga;
    private TextView tvErrorWeb;
    private Button btnAtras;
    private Button btnAdelante;

    /** Si el WebView tiene historial, "Atrás" del teléfono retrocede en la página. */
    private final OnBackPressedCallback atrasEnWeb = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            webView.goBack();
        }
    };

    public WebFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        etUrl = view.findViewById(R.id.etUrl);
        webView = view.findViewById(R.id.webView);
        pbCarga = view.findViewById(R.id.pbCarga);
        tvErrorWeb = view.findViewById(R.id.tvErrorWeb);
        btnAtras = view.findViewById(R.id.btnAtras);
        btnAdelante = view.findViewById(R.id.btnAdelante);

        configurarWebView();

        view.findViewById(R.id.btnIr).setOnClickListener(v -> cargarDesdeCaja());
        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_GO || enter) {
                cargarDesdeCaja();
                return true;
            }
            return false;
        });
        btnAtras.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
        btnAdelante.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });
        view.findViewById(R.id.btnRecargar).setOnClickListener(v -> {
            if (hayConexion()) {
                tvErrorWeb.setVisibility(View.GONE);
            }
            webView.reload();
        });
        view.findViewById(R.id.btnInicio).setOnClickListener(v -> cargar(URL_INICIO));

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            cargar(URL_INICIO);
        }
        actualizarBotones();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), atrasEnWeb);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).marcarSeccion(R.id.btnWeb);
        webView.onResume();
    }

    @Override
    public void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        webView.stopLoading();
        webView.destroy();
        webView = null;
        super.onDestroyView();
    }

    private void configurarWebView() {
        WebSettings ajustes = webView.getSettings();
        ajustes.setJavaScriptEnabled(true);
        ajustes.setDomStorageEnabled(true);
        // Ajusta páginas anchas al ancho del panel derecho.
        ajustes.setUseWideViewPort(true);
        ajustes.setLoadWithOverviewMode(true);
        ajustes.setTextZoom(85);
        ajustes.setBuiltInZoomControls(true);
        ajustes.setDisplayZoomControls(false);
        ajustes.setAllowFileAccess(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String esquema = uri.getScheme();
                if ("https".equals(esquema) || "http".equals(esquema)) {
                    return false; // Se abre dentro del WebView.
                }
                // tel:, mailto:, intent:, etc. se abren con la app correspondiente.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(requireContext(), "No hay una app para abrir este enlace",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                etUrl.setText(url);
                pbCarga.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pbCarga.setVisibility(View.GONE);
                actualizarBotones();
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                actualizarBotones();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    mostrarError(hayConexion()
                            ? "No se pudo cargar la página: " + error.getDescription()
                            : "Sin conexión a Internet. Revisa tu red e intenta de nuevo.");
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progreso) {
                pbCarga.setProgress(progreso);
                pbCarga.setVisibility(progreso < 100 ? View.VISIBLE : View.GONE);
            }
        });
    }

    /** Valida lo escrito en la caja de texto y lo carga. */
    private void cargarDesdeCaja() {
        String texto = etUrl.getText().toString().trim();
        if (texto.isEmpty()) {
            etUrl.setError("Escribe una dirección");
            return;
        }

        String url;
        if (texto.startsWith("https://")) {
            url = texto;
        } else if (texto.startsWith("http://")) {
            // Solo se permiten conexiones seguras (RNF-09).
            url = "https://" + texto.substring("http://".length());
            Toast.makeText(requireContext(), "Se usará una conexión segura (HTTPS)",
                    Toast.LENGTH_SHORT).show();
        } else if (texto.contains("://")) {
            etUrl.setError("Solo se permiten direcciones web (https)");
            return;
        } else if (!texto.contains(" ") && texto.contains(".")) {
            url = "https://" + texto;
        } else {
            // Si no parece una dirección, se busca el texto.
            url = "https://www.google.com/search?q=" + Uri.encode(texto);
        }

        if (!Patterns.WEB_URL.matcher(url).matches()) {
            etUrl.setError("La dirección no es válida");
            return;
        }
        ocultarTeclado();
        cargar(url);
    }

    private void cargar(String url) {
        if (!hayConexion()) {
            mostrarError("Sin conexión a Internet. Revisa tu red e intenta de nuevo.");
        } else {
            tvErrorWeb.setVisibility(View.GONE);
        }
        etUrl.setText(url);
        webView.loadUrl(url);
    }

    private void mostrarError(String mensaje) {
        tvErrorWeb.setText(mensaje);
        tvErrorWeb.setVisibility(View.VISIBLE);
        pbCarga.setVisibility(View.GONE);
    }

    private void actualizarBotones() {
        btnAtras.setEnabled(webView.canGoBack());
        btnAdelante.setEnabled(webView.canGoForward());
        atrasEnWeb.setEnabled(webView.canGoBack());
    }

    private boolean hayConexion() {
        ConnectivityManager cm = (ConnectivityManager) requireContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Network red = cm.getActiveNetwork();
        NetworkCapabilities capacidades = red != null ? cm.getNetworkCapabilities(red) : null;
        return capacidades != null
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
        etUrl.clearFocus();
    }
}
