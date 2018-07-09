package com.example.android.spms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public ArrayList<Float> distances       = new ArrayList<Float>();
    public ArrayList<String> parkingPlaces  = new ArrayList<String>();
    public static ArrayList<String> availableSlots = new ArrayList<String>();
    public ArrayList<String> Logitudes      = new ArrayList<String>();
    public ArrayList<String> Latitudes      = new ArrayList<String>();

    private static final String TAG = "MapsActivity";
    private int pos;
    private String status = "none",title = "";
    private double longitude = 0.0, latitude = 0.0;
    public static String empty;
    private GoogleMap mMap;
    LocationManager manager;
    private Boolean mLocationPermissionsGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private boolean mAlarm = false;
    private Location mLocation = null;
    private Timer mStopTimer = null;

    Service mservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

       /* mservice = new Service() {
            @Nullable
            @Override
            public IBinder onBind(Intent intent) {
                return null;
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {
                if (intent.getBooleanExtra("ALARM", false)) {
                    mAlarm = true;
                }

                if (!(manager.getProvider(LocationManager.NETWORK_PROVIDER))) {
                    Log.w(TAG,"network provider not enabled, will try to use last known location");
                    Location l = getLastKnownLocation();
                    if (l != null) {
                        mLocation = l;
                    } else {
                        Log.w(TAG,"unable to obtain last known location");
                    }
                    stopSelf();
                } else {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);

                    mStopTimer = new Timer();
                    mStopTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            // stop after 1 minute, regardless of
                            // whether we successfully got the location
                            // or not
                            stopSelf();
                            mStopTimer = null;
                        }
                    }, 1000 * 60);
                }

                return START_STICKY;
            }

        };*/

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        getLocationPermission();


        Button Refresh = findViewById(R.id.refresh);

        Refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent thisActivity = new Intent(MapsActivity.this, MapsActivity.class);
                thisActivity.putExtra("Status", status);

                if (status.equals("GPS")) {

                    new MyAsyncresourcesGPS().execute("http://spms.msa3d.com/api.php?get=android");

                    thisActivity.putStringArrayListExtra("parkingPlaces",parkingPlaces);
                    thisActivity.putStringArrayListExtra("Longitudes"   ,Logitudes);
                    thisActivity.putStringArrayListExtra("Latitudes"    ,Latitudes);
                    thisActivity.putStringArrayListExtra("empty"        ,availableSlots);

                    getDeviceLocation();


                } if (status.equals("List")) {

                    new MyAsyncresources().execute("http://spms.msa3d.com/api.php?get=android");

                    thisActivity.putExtra("parkingPlace", title);
                    thisActivity.putExtra("Longitude", Double.toString(longitude));
                    thisActivity.putExtra("Latitude", Double.toString(latitude));
                    thisActivity.putExtra("empty", empty);

                }

                startActivity(thisActivity);
                finish();

                Toast.makeText(getApplicationContext(), "refreshed", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void initMap() {

        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    // latitude , longitude
    // 24.129282, 32.899350 for Engineering faculty
    // 24.109652, 32.901458 for Helnan hotel
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        Intent intent = this.getIntent();
        status = intent.getStringExtra("Status");

        if (status.equals("GPS")) {

            parkingPlaces  = intent.getStringArrayListExtra("parkingPlaces");
            Logitudes      = intent.getStringArrayListExtra("Longitudes");
            Latitudes      = intent.getStringArrayListExtra("Latitudes");
            availableSlots = intent.getStringArrayListExtra("empty");

            getDeviceLocation();

        } else if (status.equals("List")) {

            longitude= Double.parseDouble(intent.getStringExtra("Longitude"));
            latitude = Double.parseDouble(intent.getStringExtra("Latitude"));
            empty    = intent.getStringExtra("empty");
            title    = intent.getStringExtra("parkingPlace");

            LatLng pos = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(pos).title(title).snippet("Available Slots: " + empty));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
        }

//        new decision().indeterminateCircularProgress("Loading data...",3000);
    }


    public void getDeviceLocation() {

//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

        distances.clear();

        int count = 0;

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Location lastLocation = getLastKnownLocation();

        //Log.e("location", "last location:" + lastLocation.toString());

        if(lastLocation != null) {

            longitude = lastLocation.getLongitude();
            latitude = lastLocation.getLatitude();

            if(parkingPlaces != null) {

                for (int i = 0; i < parkingPlaces.size(); i++) {

                    float[] result = new float[]{0};
                    Location.distanceBetween(latitude, longitude, Double.parseDouble(Latitudes.get(i)), Double.parseDouble(Logitudes.get(i)), result);
                    distances.add(result[0]);
                }
                for (int i = 0; i < distances.size(); i++) {

                    float temp;
                    String temp1;

                    for (int j = i; j < distances.size(); j++) {

                        if (distances.get(i) > distances.get(j)) {

                            temp = distances.get(j);
                            distances.set(j, distances.get(i));
                            distances.set(i, temp);

                            temp1 = Logitudes.get(j);
                            Logitudes.set(j, Logitudes.get(i));
                            Logitudes.set(i, temp1);

                            temp1 = Latitudes.get(j);
                            Latitudes.set(j, Latitudes.get(i));
                            Latitudes.set(i, temp1);

                            temp1 = parkingPlaces.get(j);
                            parkingPlaces.set(j, parkingPlaces.get(i));
                            parkingPlaces.set(i, temp1);

                            temp1 = availableSlots.get(j);
                            availableSlots.set(j, availableSlots.get(i));
                            availableSlots.set(i, temp1);


                        }
                    }
                }
            }

            // Toast.makeText(MapsActivity.this, distances.toString(), Toast.LENGTH_SHORT).show();

            LatLng myLocation = new LatLng(latitude, longitude);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;

            }

            mMap.setMyLocationEnabled(true);

            if(parkingPlaces != null) {


                for (int j = 0; j < parkingPlaces.size(); j++) {

                    if (count == 3) break;

                    if( (Integer.parseInt(availableSlots.get(j)) > 0 && status.equals("GPS")) || status.equals("List")) {

                        LatLng pos1 = new LatLng(Double.parseDouble(Latitudes.get(j)), Double.parseDouble(Logitudes.get(j)));
                        //LatLng pos2 = new LatLng(Double.parseDouble(Latitudes.get(1)), Double.parseDouble(Logitudes.get(1)));
                        //LatLng pos3 = new LatLng(Double.parseDouble(Latitudes.get(2)), Double.parseDouble(Logitudes.get(2)));

                        mMap.addMarker(new MarkerOptions().position(pos1).title(parkingPlaces.get(j)).snippet("Available Slots: " + availableSlots.get(j)));
                        //mMap.addMarker(new MarkerOptions().position(pos2).title(parkingPlaces.get(1)).snippet("Available Slots: " + availableSlots.get(1)));
                        //mMap.addMarker(new MarkerOptions().position(pos3).title(parkingPlaces.get(2)).snippet("Available Slots: " + availableSlots.get(2)));
                    }
                }
            }
            else{

                Toast.makeText(this,"Unable to get parking places",Toast.LENGTH_SHORT).show();
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));


        }else{

            Toast.makeText(this,"Unable to get your location now",Toast.LENGTH_SHORT).show();
        }

    }


    /*    @SuppressLint("MissingPermission")
        private void getDeviceLocation(){

            distances.clear();

            Log.d(TAG, "getDeviceLocation: getting the devices current location");

            // mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            Location lastLocation = null;

            if (lastLocation == null) {

                //lastLocation = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                lastLocation = getLastKnownLocation();

                Toast.makeText(this, "lastLocation = null", Toast.LENGTH_SHORT).show();
            }

            try{

                Log.d(TAG, "onComplete: found location!");

                longitude = lastLocation.getLongitude();
                latitude  = lastLocation.getLatitude();

                for (int i = 0; i < parkingPlaces.size(); i++) {

                    float[] result = new float[]{0};
                    Location.distanceBetween(latitude, longitude, Double.parseDouble(Latitudes.get(i)), Double.parseDouble(Logitudes.get(i)), result);
                    distances.add(result[0]);
                }

                for (int i = 0; i < distances.size(); i++) {

                    float temp;
                    String temp1;

                    for (int j = i; j < distances.size(); j++) {

                        if (distances.get(i) > distances.get(j)) {

                            temp = distances.get(j);
                            distances.set(j, distances.get(i));
                            distances.set(i, temp);

                            temp1 = Logitudes.get(j);
                            Logitudes.set(j, Logitudes.get(i));
                            Logitudes.set(i, temp1);

                            temp1 = Latitudes.get(j);
                            Latitudes.set(j, Latitudes.get(i));
                            Latitudes.set(i, temp1);

                            temp1 = parkingPlaces.get(j);
                            parkingPlaces.set(j, parkingPlaces.get(i));
                            parkingPlaces.set(i, temp1);

                            temp1 = availableSlots.get(j);
                            availableSlots.set(j, availableSlots.get(i));
                            availableSlots.set(i, temp1);

                        }

                    }

                }

                Toast.makeText(MapsActivity.this, distances.toString(), Toast.LENGTH_SHORT).show();

                LatLng myLocation = new LatLng(latitude, longitude);

                mMap.setMyLocationEnabled(true);

                LatLng pos1 = new LatLng(Double.parseDouble(Latitudes.get(0)), Double.parseDouble(Logitudes.get(0)));
                LatLng pos2 = new LatLng(Double.parseDouble(Latitudes.get(1)), Double.parseDouble(Logitudes.get(1)));
                LatLng pos3 = new LatLng(Double.parseDouble(Latitudes.get(2)), Double.parseDouble(Logitudes.get(2)));

                mMap.addMarker(new MarkerOptions().position(pos1).title(parkingPlaces.get(0)).snippet("Available Slots: " + availableSlots.get(0)));
                mMap.addMarker(new MarkerOptions().position(pos2).title(parkingPlaces.get(1)).snippet("Available Slots: " + availableSlots.get(1)));
                mMap.addMarker(new MarkerOptions().position(pos3).title(parkingPlaces.get(2)).snippet("Available Slots: " + availableSlots.get(2)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos1, 13));


            }catch (SecurityException e){

                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
            }

        }
    */
    private Location getLastKnownLocation() {

        List<String> providers = manager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {

            @SuppressLint("MissingPermission") Location l = manager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    String Data;

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class MyAsyncresources extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            Data = "";
        }

        protected String doInBackground(String... params) {

            try {

                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Data = Stream2String(in);
                //publishProgress(parkingPlaces);
                in.close();

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                publishProgress("there is an error");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            Toast.makeText(MapsActivity.this, values[0], Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(String value) {

            //Toast.makeText(decision.this, Data, Toast.LENGTH_SHORT).show();

            JSONObject data;

            try {

                data = new JSONObject(Data);

                JSONArray JA = data.getJSONArray("lot");

                JSONObject json = JA.getJSONObject(pos);
                empty   = (json.getString("empty"));
                Intent i = new Intent(MapsActivity.this,MapsActivity.class);
                i.putExtra("empty", empty);

                //Toast.makeText(MapsActivity.this, empty ,Toast.LENGTH_SHORT).show();


            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }
    public class MyAsyncresourcesGPS extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            Data = "";

        }

        protected String doInBackground(String... params) {

            try {

                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Data = Stream2String(in);
                //publishProgress(parkingPlaces);
                in.close();

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                publishProgress("there is an error");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            Toast.makeText(MapsActivity.this, values[0], Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(String value) {

            //Toast.makeText(decision.this, Data, Toast.LENGTH_SHORT).show();

            JSONObject data;

            try {

                availableSlots.clear();

                data = new JSONObject(Data);

                JSONArray JA = data.getJSONArray("lot");

                for (int i = 0; i < JA.length(); i++) {

                    JSONObject json = JA.getJSONObject(i);
                    availableSlots.add(json.getString("empty"));
                    //Toast.makeText(decision.this, parkingPlaces.get(i) + " ",Toast.LENGTH_SHORT).show();

                }
                Intent ii = new Intent(MapsActivity.this,MapsActivity.class);
                ii.putStringArrayListExtra("empty"        ,availableSlots);


                //Toast.makeText(MapsActivity.this, empty ,Toast.LENGTH_SHORT).show();


            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }
    public String Stream2String(InputStream inputStream) {

        BufferedReader bureader = new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String Text="";
        try{
            while((line=bureader.readLine())!=null) {
                Text+=line;
            }
            inputStream.close();
        }catch (Exception ex){}
        return Text;
    }


}
