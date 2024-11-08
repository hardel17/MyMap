package com.cdp.mymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    EditText txtLatitud, txtLongitud;
    Button btnBuscar;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final long UPDATE_INTERVAL = 30000; // 30 segundos
    private Handler handler = new Handler();
    private boolean shouldUpdateLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        btnBuscar = findViewById(R.id.btnBuscar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            obtenerUbicacionActual();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnBuscar.setOnClickListener(v -> {
            buscarUbicacionManual();
            iniciarActualizacionUbicacion();
        });
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                        mostrarUbicacionEnMapa(ubicacionActual);
                        txtLatitud.setText(String.valueOf(location.getLatitude()));
                        txtLongitud.setText(String.valueOf(location.getLongitude()));
                    }
                }
            });
        }
    }

    private void mostrarUbicacionEnMapa(LatLng ubicacion) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(ubicacion).title("Ubicación actual"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
        }
    }

    private void buscarUbicacionManual() {
        String latitudStr = txtLatitud.getText().toString();
        String longitudStr = txtLongitud.getText().toString();

        if (latitudStr != null && !latitudStr.isEmpty() && longitudStr != null && !longitudStr.isEmpty()) {
            try {
                double lat = Double.parseDouble(latitudStr);
                double lng = Double.parseDouble(longitudStr);
                LatLng ubicacion = new LatLng(lat, lng);
                mostrarUbicacionEnMapa(ubicacion);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor ingrese coordenadas válidas", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Por favor ingrese valores de latitud y longitud", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarActualizacionUbicacion() {
        shouldUpdateLocation = true;
        handler.postDelayed(updateLocationRunnable, UPDATE_INTERVAL);
    }

    private final Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (shouldUpdateLocation) {
                obtenerUbicacionActual(); // Actualizar ubicación
                shouldUpdateLocation = false; // Detener actualizaciones automáticas después de la primera
            }
        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        // Habilitar la capa de ubicación si los permisos están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        txtLatitud.setText(String.valueOf(latLng.latitude));
        txtLongitud.setText(String.valueOf(latLng.longitude));
        mostrarUbicacionEnMapa(latLng);
        iniciarActualizacionUbicacion(); // Iniciar actualización de ubicación
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        txtLatitud.setText(String.valueOf(latLng.latitude));
        txtLongitud.setText(String.valueOf(latLng.longitude));
        mostrarUbicacionEnMapa(latLng);
        iniciarActualizacionUbicacion(); // Iniciar actualización de ubicación
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateLocationRunnable); // Detener el ciclo de actualización cuando se cierre la app
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
