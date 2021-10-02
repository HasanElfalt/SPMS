package com.example.android.spms;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;

import android.os.Handler;

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


public class decision extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static ArrayList<String> parkingPlaces = new ArrayList<String>();
    public static ArrayList<String> empty         = new ArrayList<String>();
    public static ArrayList<String> Logitude      = new ArrayList<String>();
    public static ArrayList<String> Latitude      = new ArrayList<String>();

    private String status = "none";
    public  static int pos;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    boolean spinner;
    private static final String TAG = "decision";

    final Handler mHandler = new Handler();


    RadioGroup radioGroup;
    RadioButton List, GPS;
    Button toMaps;

    ArrayAdapter<String> adapter;
    Spinner spinner1;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new MyAsyncresources().execute("http://spms.msa3d.com/api.php?get=android");

        spinner = true;

        indeterminateCircularProgress("",3000);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        toMaps = findViewById(R.id.ToMap);
        radioGroup = findViewById(R.id.Radio);
        List = findViewById(R.id.list);
        GPS = findViewById(R.id.fromGPS);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (GPS.isChecked()) {

                    status = "GPS";
                    spinner1.setVisibility(View.INVISIBLE);

                } else if (List.isChecked()) {

                    status = "List";
                    spinner1.setVisibility(View.VISIBLE);

                } else {

                    spinner1.setVisibility(View.INVISIBLE);

                }

            }
        });

        if(isServicesOK()) {

            toMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (status.equals("GPS")) {

                        indeterminateCircularProgress("Waiting...", 3000);

                        statusCheck();

                    } else if (status.equals("List")) {

                        Intent intent = new Intent(decision.this, MapsActivity.class);
                        intent.putExtra("Status", status);
                        intent.putExtra("parkingPlace", parkingPlaces.get(pos));
                        intent.putExtra("Longitude", Logitude.get(pos));
                        intent.putExtra("Latitude", Latitude.get(pos));
                        intent.putExtra("empty", empty.get(pos));
                        Toast.makeText(decision.this, "Status:" + status, Toast.LENGTH_SHORT).show();
                        startActivity(intent);

                    }

                }

            });

        }


        spinner1 = findViewById(R.id.places);

    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(decision.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(decision.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();

        }else{

            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // that's for overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    // that's for overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.log_out:

                FirebaseAuth.getInstance().signOut();
                Intent back_to_log_in = new Intent(decision.this, Home.class);
                startActivity(back_to_log_in);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void indeterminateCircularProgress(String message, final long millis ) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait...");
        dialog.setMessage(message);
        dialog.setIndeterminate(true); //Change the indeterminate (غير محدد)mode for this ProgressDialog.
        dialog.setCancelable(false); //Sets whether this dialog is cancelable with the BACK key.
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis); // Here we can place our time consuming task
                    dismissDialog(dialog);
                } catch (Exception e) {
                    dismissDialog(dialog);
                }

            }
        }).start();


    }

    public void dismissDialog(final ProgressDialog pd) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
            }
        });
    }


    public void statusCheck() {

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationSettingsRequest(this);

        } else {

            Intent intent = new Intent(decision.this, MapsActivity.class);
            intent.putExtra("Status", status);
            intent.putStringArrayListExtra("parkingPlaces", parkingPlaces);
            intent.putStringArrayListExtra("Longitudes", Logitude);
            intent.putStringArrayListExtra("Latitudes", Latitude);
            intent.putStringArrayListExtra("empty", empty);
            Toast.makeText(decision.this, "Status:" + status, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    }

    private void displayLocationSettingsRequest(Context context) {

        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5 * 1000);
            locationRequest.setFastestInterval(5 * 1000 / 2);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            toast("Success");
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            toast("GPS is not on");
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(decision.this, 1000);

                            } catch (IntentSender.SendIntentException e) {

                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            toast("Setting change not allowed");
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                        default:
                            toast("Nothing");
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        indeterminateCircularProgress("",2000);
                    }
                }, 2000);

                Intent intent = new Intent(decision.this, MapsActivity.class);
                intent.putExtra("Status", status);
                intent.putStringArrayListExtra("parkingPlaces" ,parkingPlaces);
                intent.putStringArrayListExtra("Longitudes"    ,Logitude);
                intent.putStringArrayListExtra("Latitudes"     ,Latitude);
                intent.putStringArrayListExtra("empty"         ,empty);
                Toast.makeText(decision.this, "Status:" + status, Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        toast("Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast("Failed");
    }

    private void toast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {

        }
    }

    String Data;

    public class MyAsyncresources extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            Data = "";

            parkingPlaces.clear();
            Latitude.clear();
            Logitude.clear();
            empty.clear();
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

            Toast.makeText(decision.this, values[0], Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(String result2) {

            //Toast.makeText(decision.this, Data, Toast.LENGTH_SHORT).show();

            JSONObject data;

            try {

                data = new JSONObject(Data);

                JSONArray JA = data.getJSONArray("lot");

                for (int i = 0; i < JA.length(); i++) {

                    JSONObject json = JA.getJSONObject(i);
                    parkingPlaces.add(json.getString("location"));
                    empty.add(json.getString("empty"));
                    Logitude.add(json.getString("lng"));
                    Latitude.add(json.getString("lat"));
                    //Toast.makeText(decision.this, parkingPlaces.get(i) + " " + Logitude.get(i) + " " + Latitude.get(i) ,Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            if(spinner) {

                spinner = false;

                adapter = new ArrayAdapter<String>(decision.this, android.R.layout.simple_spinner_item, parkingPlaces);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner1.setAdapter(adapter);

                spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

                    // adapterView: The AdapterView where the selection happened
                    // View: The view within the AdapterView that was clicked
                    // int position: The position of the view in the adapter
                    // long id: The row id of the item that is selected


                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                        pos = position;

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
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