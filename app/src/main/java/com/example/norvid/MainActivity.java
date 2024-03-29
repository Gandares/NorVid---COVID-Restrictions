package com.example.norvid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private final FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "DocSnippets";
    public static final int DEFAULT_UPDATE_INTERVAL = 20;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static String[] MUNLIST;
    private String MunViewText;
    private String ProvViewText;
    private String CCAAViewText;
    private boolean firstTime = true;
    private final String nohay = "No hay.";
    private final String ned = "No existen datos.";
    private final String eelp = "Error en la petición.";
    int lid = 1000;
    List<TextView> infos = new ArrayList<TextView>();
    List<TextView> infosCCAA = new ArrayList<TextView>();
    List<TextView> infosProv = new ArrayList<TextView>();
    //TextView ccaa, mun, prov;


    TextView bbddtdqA, bbddcpA, bbddrA, bbddhmhA, bbddiA, bbddeA, bbddhmonA;

    TextView pbbddtdqA, pbbddcpA, pbbddrA, pbbddhmhA, pbbddiA, pbbddeA, pbbddhmonA;

    TextView mbbddtdqA, mbbddcpA, mbbddrA, mbbddhmhA, mbbddiA, mbbddeA, mbbddhmonA;

    TextView CargandoCCAA, CargandoProv, CargandoMun;

    BottomNavigationView botNav;
    String municipio, provincia, comunidadAutonoma;
    AutoCompleteTextView ccaaView, ProvView, MunView;
    Button loadButton, buttonp;


    Timer timer;


    // Google's API for location service
    FusedLocationProviderClient fusedLocationProviderClient;

    // Location request
    LocationRequest locationRequest;

    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ccaa = findViewById(R.id.ccaa);
        prov = findViewById(R.id.prov);
        mun = findViewById(R.id.mun);*/
/*
        bbddtdqA = findViewById(R.id.bbddtdqA);
        bbddcpA = findViewById(R.id.bbddcpA);
        bbddrA = findViewById(R.id.bbddrA);
        bbddhmhA = findViewById(R.id.bbddhmhA);
        bbddiA = findViewById(R.id.bbddiA);
        bbddeA = findViewById(R.id.bbddeA);
        bbddhmonA = findViewById(R.id.bbddhmonA);

        pbbddtdqA = findViewById(R.id.pbbddtdqA);
        pbbddcpA = findViewById(R.id.pbbddcpA);
        pbbddrA = findViewById(R.id.pbbddrA);
        pbbddhmhA = findViewById(R.id.pbbddhmhA);
        pbbddiA = findViewById(R.id.pbbddiA);
        pbbddeA = findViewById(R.id.pbbddeA);
        pbbddhmonA = findViewById(R.id.pbbddhmonA);*/

        /*mbbddtdqA = findViewById(R.id.mbbddtdqA);
        mbbddcpA = findViewById(R.id.mbbddcpA);
        mbbddrA = findViewById(R.id.mbbddrA);
        mbbddhmhA = findViewById(R.id.mbbddhmhA);
        mbbddiA = findViewById(R.id.mbbddiA);
        mbbddeA = findViewById(R.id.mbbddeA);
        mbbddhmonA = findViewById(R.id.mbbddhmonA);*/

        botNav = findViewById(R.id.bottom_navigation);
        ccaaView = findViewById(R.id.ccaaView);
        ProvView = findViewById(R.id.ProvView);
        MunView = findViewById(R.id.MunView);
        loadButton = findViewById(R.id.loadButton);
        loadButton.setEnabled(false);

        CargandoCCAA = findViewById(R.id.CargandoCCAA);
        CargandoProv = findViewById(R.id.CargandoProv);
        CargandoMun = findViewById(R.id.CargandoMun);

        // set all properties of LocationRequest
        locationRequest = LocationRequest.create();
        // How often does the location check occur when set to the most frequent update
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // How often does the default location check occur
        locationRequest.setInterval(FAST_UPDATE_INTERVAL * 1000);
        // Event that is triggered whenever the update interval is met
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateUIValues(location);
                        if(firstTime==true) {
                            loadButton.setEnabled(true);
                            showData();
                            firstTime = false;
                        }
                    }
                }
            }
        };
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }

        // Evento google analytics
        Bundle bundle = new Bundle();
        bundle.putString("message", "Aplicación abierta");
        mFirebaseAnalytics.logEvent("InitScreen", bundle);

        /*municipio = mun.getText().toString();
        provincia = prov.getText().toString();
        comunidadAutonoma = ccaa.getText().toString();*/

        botNav.setOnNavigationItemSelectedListener( item -> {
            return navigation(item);
        });

        db.collection("Listas").document("CCAA").get()
            .addOnSuccessListener(documentSnapshot -> {
                ArrayList<String> ccaaList = (ArrayList<String>) documentSnapshot.getData().get("lista");
                String[] arrayAdapter = new String[ccaaList.size()];
                for (int i = 0; i < ccaaList.size(); i++)
                    arrayAdapter[i] = ccaaList.get(i);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayAdapter);
                ccaaView.setAdapter(adapter);

                ccaaView.setOnItemClickListener((parent, view, position, id) -> {
                    CCAAViewText = parent.getItemAtPosition(position).toString();
                    comunidadAutonoma = CCAAViewText;
                });
            });

        db.collection("Listas").document("Provincias").get()
            .addOnSuccessListener(documentSnapshot -> {
                ArrayList<String> provList = (ArrayList<String>) documentSnapshot.getData().get("lista");
                String[] arrayAdapter = new String[provList.size()];
                for (int i = 0; i < provList.size(); i++)
                    arrayAdapter[i] = provList.get(i);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayAdapter);
                ProvView.setAdapter(adapter);

                ProvView.setOnItemClickListener((parent, view, position, id) -> {
                    ProvViewText = parent.getItemAtPosition(position).toString();
                    provincia = ProvViewText;
                });
            });


        db.collection("Listas").document("Municipios").get()
            .addOnSuccessListener(documentSnapshot -> {
                ArrayList<String> munList = (ArrayList<String>) documentSnapshot.getData().get("lista");
                String[] arrayAdapter = new String[munList.size()];
                for (int i = 0; i < munList.size(); i++)
                    arrayAdapter[i] = munList.get(i);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayAdapter);
                MunView.setAdapter(adapter);

                MunView.setOnItemClickListener((parent, view, position, id) -> {
                    MunViewText = parent.getItemAtPosition(position).toString();
                    municipio = MunViewText;
                });
            });


        loadButton.setOnClickListener(v -> showData());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGPSLoop();
            }
            else{
                Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private void startGPSLoop(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateUIValues(Location location){
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this);

            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if(MunViewText == null || MunViewText.isEmpty())
                municipio = addresses.get(0).getLocality();
            if(ProvViewText == null || ProvViewText.isEmpty())
                provincia = addresses.get(0).getSubAdminArea();
            if(CCAAViewText == null || CCAAViewText.isEmpty())
                comunidadAutonoma = addresses.get(0).getAdminArea();

            MunView.setHint(municipio);
            ProvView.setHint(provincia);
            ccaaView.setHint(comunidadAutonoma);

        } catch(Exception e){

        }
    }


    private void showData() {
        if(!infos.isEmpty()){
            ConstraintLayout layout = (ConstraintLayout) CargandoMun.getParent();
            for(TextView r : infos){
                layout.removeView(r);
            }
        }
        if(!infosCCAA.isEmpty()){
            ConstraintLayout layout = (ConstraintLayout) CargandoCCAA.getParent();
            for(TextView r : infosCCAA){
                layout.removeView(r);
            }
        }
        if(!infosProv.isEmpty()){
            ConstraintLayout layout = (ConstraintLayout) CargandoProv.getParent();
            for(TextView r : infosProv){
                layout.removeView(r);
            }
        }
        infos.clear();
        infosCCAA.clear();
        infosProv.clear();
        lid = 1000;

        CollectionReference docRef = db.collection("Municipios").document(municipio).collection("Restricciones");
        CollectionReference docRefProv = db.collection("Provincias").document(provincia).collection("Restricciones");
        CollectionReference docRefCCAA = db.collection("CCAA").document(comunidadAutonoma).collection("Restricciones");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                try {
                    DocumentSnapshot totry = documents.get(0);
                    ConstraintLayout layout = (ConstraintLayout) CargandoMun.getParent();
                    int idlast = -1;
                    for (int i = 0; i < documents.size(); i++) {
                        boolean first = true;
                        long datalen = (long) documents.get(i).getData().size();
                        for (int j = 0; j < datalen; j++) {
                            ConstraintSet set = new ConstraintSet();
                            TextView restriction = new TextView(MainActivity.this);
                            restriction.setId(lid);
                            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            layout.addView(restriction, lp);
                            if ((String) documents.get(i).getData().get(Integer.toString(j)) != null) {
                                restriction.setText((String) documents.get(i).getData().get(Integer.toString(j)));
                            } else {
                                restriction.setText((String) documents.get(i).getData().get("data" + j));
                            }
                            restriction.setTextSize(14);
                            restriction.setTextColor(Color.parseColor("#BBBBBB"));
                            set.clone(layout);
                            if (idlast == -1) {
                                set.connect(restriction.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                                set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                first = false;
                            } else {
                                if (first) {
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.BOTTOM);
                                    set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                    first = false;
                                } else {
                                    set.connect(restriction.getId(), ConstraintSet.START, idlast, ConstraintSet.END);
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.TOP);
                                    set.connect(restriction.getId(), ConstraintSet.BOTTOM, idlast, ConstraintSet.BOTTOM);
                                }
                            }
                            set.applyTo(layout);
                            idlast = lid;
                            lid++;
                            infos.add(restriction);
                            CargandoMun.setText("");
                        }
                    }
                } catch (Exception err) {
                    CargandoMun.setText("Ninguna restricción activa");
                }
            }
        });
        docRefCCAA.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                try {
                    DocumentSnapshot totry = documents.get(0);
                    ConstraintLayout layout = (ConstraintLayout) CargandoCCAA.getParent();
                    int idlast = -1;
                    for (int i = 0; i < documents.size(); i++) {
                        boolean first = true;
                        long datalen = (long) documents.get(i).getData().size();
                        for (int j = 0; j < datalen; j++) {
                            ConstraintSet set = new ConstraintSet();
                            TextView restriction = new TextView(MainActivity.this);
                            restriction.setId(lid);
                            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            layout.addView(restriction, lp);
                            if ((String) documents.get(i).getData().get(Integer.toString(j)) != null) {
                                restriction.setText((String) documents.get(i).getData().get(Integer.toString(j)));
                            } else {
                                restriction.setText((String) documents.get(i).getData().get("data" + j));
                            }
                            restriction.setTextSize(14);
                            restriction.setTextColor(Color.parseColor("#BBBBBB"));
                            set.clone(layout);
                            if (idlast == -1) {
                                set.connect(restriction.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                                set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                first = false;
                            } else {
                                if (first) {
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.BOTTOM);
                                    set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                    first = false;
                                } else {
                                    set.connect(restriction.getId(), ConstraintSet.START, idlast, ConstraintSet.END);
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.TOP);
                                    set.connect(restriction.getId(), ConstraintSet.BOTTOM, idlast, ConstraintSet.BOTTOM);
                                }
                            }
                            set.applyTo(layout);
                            idlast = lid;
                            lid++;
                            infos.add(restriction);
                            CargandoCCAA.setText("");
                        }
                    }
                } catch (Exception err) {
                    CargandoCCAA.setText("Ninguna restricción activa");
                }
            }
        });
        docRefProv.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                try {
                    DocumentSnapshot totry = documents.get(0);
                    ConstraintLayout layout = (ConstraintLayout) CargandoProv.getParent();
                    int idlast = -1;
                    for (int i = 0; i < documents.size(); i++) {
                        boolean first = true;
                        long datalen = (long) documents.get(i).getData().size();
                        for (int j = 0; j < datalen; j++) {
                            ConstraintSet set = new ConstraintSet();
                            TextView restriction = new TextView(MainActivity.this);
                            restriction.setId(lid);
                            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            layout.addView(restriction, lp);
                            if ((String) documents.get(i).getData().get(Integer.toString(j)) != null) {
                                restriction.setText((String) documents.get(i).getData().get(Integer.toString(j)));
                            } else {
                                restriction.setText((String) documents.get(i).getData().get("data" + j));
                            }
                            restriction.setTextSize(14);
                            restriction.setTextColor(Color.parseColor("#BBBBBB"));
                            set.clone(layout);
                            if (idlast == -1) {
                                set.connect(restriction.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                                set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                first = false;
                            } else {
                                if (first) {
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.BOTTOM);
                                    set.connect(restriction.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 50);
                                    first = false;
                                } else {
                                    set.connect(restriction.getId(), ConstraintSet.START, idlast, ConstraintSet.END);
                                    set.connect(restriction.getId(), ConstraintSet.TOP, idlast, ConstraintSet.TOP);
                                    set.connect(restriction.getId(), ConstraintSet.BOTTOM, idlast, ConstraintSet.BOTTOM);
                                }
                            }
                            set.applyTo(layout);
                            idlast = lid;
                            lid++;
                            infos.add(restriction);
                            CargandoProv.setText("");
                        }
                    }
                } catch (Exception err) {
                    CargandoProv.setText("Ninguna restricción activa");
                }
            }
        });
    }

    public boolean navigation(MenuItem item){
        switch (item.getItemId()) {
            case R.id.coronavirus:
                Log.d(TAG, "corona");
                return true;

            case R.id.login:
                Log.d(TAG, "upload");
                Intent loginIntent = new Intent(this, loginActivity.class);
                this.startActivity(loginIntent);
                return true;

            default:
                return true;
        }
    }
}