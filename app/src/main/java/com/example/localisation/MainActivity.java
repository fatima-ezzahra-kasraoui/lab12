package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ─── Constantes ───────────────────────────────────────────────────────────
    private static final int REQ_LOC = 100;

    // ⚠️ Remplacer par l'IP du PC serveur (même Wi-Fi que le téléphone)
    // Émulateur Android : utiliser 10.0.2.2
    private static final String INSERT_URL =
            "http://192.168.1.1/localisation/createPosition.php";

    // ─── UI ───────────────────────────────────────────────────────────────────
    private TextView tvLat, tvLon, tvAlt, tvAccuracy, tvServer;

    // ─── Services ─────────────────────────────────────────────────────────────
    private RequestQueue  requestQueue;
    private LocationManager locationManager;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding vues
        tvLat      = findViewById(R.id.tvLat);
        tvLon      = findViewById(R.id.tvLon);
        tvAlt      = findViewById(R.id.tvAlt);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvServer   = findViewById(R.id.tvServer);
        Button btnMap = findViewById(R.id.btnMap);

        // Init Volley
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Init LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Navigation vers la carte
        btnMap.setOnClickListener(v ->
                startActivity(new Intent(this, MapsActivity.class)));

        // Demande permission + démarrage GPS
        askLocationPermissionAndStart();
    }

    // ─── Permissions ──────────────────────────────────────────────────────────
    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─── GPS ──────────────────────────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        tvServer.setText("🛰 GPS actif — en attente du signal…");

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60_000,   // 1 minute minimum entre 2 updates
                150,      // 150 mètres minimum
                locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();
            float  acc = location.getAccuracy();

            // Mise à jour UI
            tvLat.setText(String.format(Locale.getDefault(), "Latitude : %.6f°", lat));
            tvLon.setText(String.format(Locale.getDefault(), "Longitude : %.6f°", lon));
            tvAlt.setText(String.format(Locale.getDefault(), "%.1f m", alt));
            tvAccuracy.setText(String.format(Locale.getDefault(), "± %.1f m", acc));

            // Toast avec le message du lab
            String msg = String.format(
                    getString(R.string.new_location), lat, lon, alt, acc);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

            // Envoi serveur
            tvServer.setText("⏳ Envoi en cours…");
            addPosition(lat, lon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String newStatus;
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:        newStatus = "Hors service";       break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE: newStatus = "Indisponible";     break;
                case LocationProvider.AVAILABLE:             newStatus = "Disponible";          break;
                default:                                     newStatus = "Inconnu";
            }
            String msg = String.format(getString(R.string.provider_new_status), provider, newStatus);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Toast.makeText(getApplicationContext(),
                    String.format(getString(R.string.provider_enabled), provider),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(getApplicationContext(),
                    String.format(getString(R.string.provider_disabled), provider),
                    Toast.LENGTH_SHORT).show();
        }
    };

    // ─── Envoi HTTP ───────────────────────────────────────────────────────────
    private void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                INSERT_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean ok = json.optBoolean("ok", false);
                        String ip  = json.optString("ip", "");
                        tvServer.setText(ok
                                ? "✅ Enregistré  |  IP : " + ip
                                : "❌ " + json.optString("error", "Erreur serveur"));
                    } catch (JSONException e) {
                        tvServer.setText("✅ Position envoyée");
                    }
                },
                error -> tvServer.setText("❌ Erreur réseau : " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                params.put("latitude",  String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date",      sdf.format(new Date()));
                params.put("imei",      getDeviceIdentifier());
                return params;
            }
        };
        requestQueue.add(request);
    }

    // ─── Identifiant appareil ─────────────────────────────────────────────────
    private String getDeviceIdentifier() {
        // 1) ANDROID_ID — stable, pas de permission spéciale
        String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) return androidId;

        // 2) Fallback IMEI pour anciens Android (nécessite READ_PHONE_STATE)
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    @SuppressLint("HardwareIds")
                    String imei = tm.getDeviceId();
                    if (imei != null && !imei.trim().isEmpty()) return imei;
                }
            }
        } catch (Exception ignored) {}

        return "UNKNOWN_DEVICE";
    }

    // ─── Cycle de vie ─────────────────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }
}
