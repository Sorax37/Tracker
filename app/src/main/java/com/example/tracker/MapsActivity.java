package com.example.tracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.database.FirebaseDatabase;

import android.location.LocationManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import java.lang.Object;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PopupMenu.OnMenuItemClickListener

{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location location;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    List<LatLng> point = new ArrayList<LatLng>();

    private StorageReference mStorageRef;
    FirebaseStorage storage;
    FirebaseDatabase database;

    private Button btnStart;
    private Button btnStop;
    private Button btnRoutes;

    final String LOG_TAG = "myLogs";

    LocationManager locationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnStart = findViewById(R.id.btnstart);
        btnStop = findViewById(R.id.btnstop);
        btnRoutes = findViewById(R.id.btnroutes);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startonclick();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stoponclick();
            }
        });

        btnRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showpopupmenu(v);
            }
        });


        storage =  FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

    }




    public void startonclick()
    {
        mMap.clear();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void stoponclick()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        mMap.clear();
        writeFile();
    }

    private void showpopupmenu(View v)
    {
        File path = getFilesDir();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd'at'HH:mm");
        File file = new File(path,dateFormat.format(date) + ".txt");
        File[] listOfFiles = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File path, String file) {
                return file.toLowerCase().endsWith(".txt");
            }
        });
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        for (int i = 0; i < listOfFiles.length; i++)
        {
            popupMenu.getMenu().add(Menu.NONE, i + 1, i + 1, listOfFiles[i].getName());
        }
        Log.d(LOG_TAG, Integer.toString(listOfFiles.length));
        popupMenu.show();

    }

     void writeFile() {
        File path = getFilesDir();
        if (path.exists()) {
            try {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd'at'HH:mm");
                File file = new File(path, dateFormat.format(date) + ".txt");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        openFileOutput(file.getName(), MODE_PRIVATE)));
                for (int i = 0; i < point.size(); i++) {
                    String lat = Double.toString(point.get(i).latitude);
                    String lng = Double.toString(point.get(i).longitude);

                    bw.write(lat + "\n");
                    bw.write(lng + "\n");
                }
                bw.write("");
                bw.close();
                Log.d(LOG_TAG, "Файл записан");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            path.mkdirs();
            try {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd'at'HH:mm");
                File file = new File(path, dateFormat.format(date) + ".txt");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        openFileOutput(file.getName(), MODE_PRIVATE)));
                for (int i = 0; i < point.size(); i++) {
                    String lat = Double.toString(point.get(i).latitude);
                    String lng = Double.toString(point.get(i).longitude);

                    bw.write(lat + "\n");
                    bw.write(lng + "\n");
                }
                bw.write("");
                bw.close();
                Log.d(LOG_TAG, "Файл записан");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        point.clear();
         Toast.makeText(this, "Маршрут сохранен", Toast.LENGTH_SHORT).show();
    }

    public void deletefile(MenuItem item)
    {
        File path = getFilesDir();
        CharSequence name = item.getTitle();

        File file = new File(path, String.valueOf(name));
        file.delete();

    }

    public void readFile(MenuItem item) {
        try {
            File path = getFilesDir();
            CharSequence name = item.getTitle();
            File file = new File(path, String.valueOf(name));
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(file.getName())));
            for (; ;)
            {
                String lat, lng;
                lat = br.readLine();
                lng = br.readLine();
                if (lat == null || lng == null) break;
                double dlat = Double.parseDouble(lat);
                double dlng = Double.parseDouble(lng);
                point.add(new LatLng(dlat, dlng));
            }
            Log.d(LOG_TAG, Integer.toString(point.size()));
            PolylineOptions polylineOptions = new PolylineOptions().addAll(point).color(Color.GREEN).width(15);
            mMap.addPolyline(polylineOptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    public boolean checkUserLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {

        point.add(new LatLng(location.getLatitude(),location.getLongitude()));
        PolylineOptions polylineOptions = new PolylineOptions().addAll(point).color(Color.BLUE).width(15);
        mMap.addPolyline(polylineOptions);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        point.clear();
        mMap.clear();
        readFile(item);
        //deletefile(item);
        point.clear();
       return true;
    }
}
