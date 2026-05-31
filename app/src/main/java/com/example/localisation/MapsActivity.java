package com.example.localisation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // ─── Constantes ───────────────────────────────────────────────────────────
    // ⚠️ Remplacer par l'IP du PC serveur
    private static final String SHOW_URL =
            "http://192.168.1.1/localisation/showPositions.php";

    // ─── Membres ──────────────────────────────────────────────────────────────
    private GoogleMap     mMap;
    private RequestQueue  requestQueue;
    private TextView      tvMarkerCount;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvMarkerCount = findViewById(R.id.tvMarkerCount);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Init fragment carte
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnRefresh.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.clear();
                loadPositions();
            }
        });
    }

    // ─── Carte prête ──────────────────────────────────────────────────────────
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        loadPositions();
    }

    // ─── Chargement des positions ─────────────────────────────────────────────
    private void loadPositions() {
        tvMarkerCount.setText("⏳ Chargement…");

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                SHOW_URL,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");
                        int count = positions.length();

                        if (count == 0) {
                            tvMarkerCount.setText("Aucune position enregistrée");
                            return;
                        }

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        for (int i = 0; i < count; i++) {
                            JSONObject pos  = positions.getJSONObject(i);
                            double lat  = pos.getDouble("latitude");
                            double lon  = pos.getDouble("longitude");
                            String date = pos.optString("date", "");
                            String imei = pos.optString("imei", "");

                            LatLng point = new LatLng(lat, lon);

                            // Marqueur coloré : premier = rouge, autres = bleu
                            float couleur = (i == 0)
                                    ? BitmapDescriptorFactory.HUE_RED
                                    : BitmapDescriptorFactory.HUE_AZURE;

                            mMap.addMarker(new MarkerOptions()
                                    .position(point)
                                    .title("Position " + (i + 1))
                                    .snippet("🕐 " + date + "\n📱 " + imei)
                                    .icon(BitmapDescriptorFactory.defaultMarker(couleur)));

                            boundsBuilder.include(point);
                        }

                        // Zoom automatique sur tous les marqueurs
                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                        boundsBuilder.build(), 120));

                        tvMarkerCount.setText("📍 " + count + " position(s) affichée(s)");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvMarkerCount.setText("❌ Erreur JSON");
                        Toast.makeText(this, "Erreur JSON : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    tvMarkerCount.setText("❌ Erreur réseau");
                    Toast.makeText(this, "Erreur réseau : " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(req);
    }
}
