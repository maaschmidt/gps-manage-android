package com.mudandominhaposicaogps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private LocationManager locationManager = null;
    private SensorManager sensorManager = null;

    private TextView tvPosicaoInicial = null;
    private TextView tvPosicaoAtual = null;
    private TextView tvDistancia = null;
    private Button btnAtulizarPosicao = null;
    private Button btnCalcularDistancia = null;
    private Location initialLocation = null;
    private Location pinedLocation = null;
    ArrayList<Float> sensorEvent = new ArrayList<Float>();;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        tvPosicaoInicial = (TextView) findViewById(R.id.tv_position_inicial);
        tvPosicaoAtual = (TextView) findViewById(R.id.tv_actual_position);
        tvDistancia = (TextView) findViewById(R.id.tv_distance);
        btnAtulizarPosicao = (Button) findViewById(R.id.btn_atual_position);
        btnCalcularDistancia = (Button) findViewById(R.id.btn_calculate);

        atualizaLocalizacao(tvPosicaoInicial);

        btnAtulizarPosicao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atualizaLocalizacao(tvPosicaoAtual);
            }
        });

        btnCalcularDistancia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculaDistancia();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Magnetic
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorEvent.clear();

        sensorEvent.add(event.values[0]);
        sensorEvent.add(event.values[1]);
        sensorEvent.add(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void atualizaLocalizacao(TextView textView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    null,
                    getMainExecutor(),
                    new Consumer<Location>() {
                        @Override
                        public void accept(Location location) {

                            if (textView == tvPosicaoInicial) {
                                initialLocation = location;
                            } else {
                                pinedLocation = location;
                            }

                            String bussolaText = "";

                            if (!sensorEvent.isEmpty()){
                                bussolaText =
                                        "\n>> Bússola <<\n" +
                                                "X: " + sensorEvent.get(0) + "\n" +
                                                "Y: " + sensorEvent.get(1) + "\n" +
                                                "Z: " + sensorEvent.get(2);
                            }

                            String text =
                                    "Acuracia: " + location.getAccuracy() + "\n" +
                                    "Altitude: " + location.getAltitude() + "\n" +
                                    "Latitude: " + location.getLatitude() + "\n" +
                                    "Longitude: " + location.getLatitude() + "\n" +
                                    bussolaText;

                            textView.setText(text);
                        }
                    });
        }
    }

    private void calculaDistancia(){
        if (pinedLocation == null) {
            Toast.makeText(this,"É necessário marcar um posição atual.", Toast.LENGTH_LONG).show();
            return;
        }

        initialLocation.distanceTo(pinedLocation);

        String text = "A distância entre a posição inicial e a posição maracada é de " + initialLocation.distanceTo(pinedLocation) + " metros";
        tvDistancia.setText(text);
    }
}