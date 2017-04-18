package com.example.topsu.pysakki;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    Locator locator;

    double test_latitude = 60.4615937;
    double test_longitude = 22.2240839;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void getPysakki(View v) {
        final Pysakki Pysakki = new Pysakki();
        Locator locator = new Locator(mGoogleApiClient, this);
        final double longitude = locator.getLongitude();
        final double latitude = locator.getLatitude();

        VolleyRequest.makeVolleyRequest(this, "http://data.foli.fi/gtfs/stops", new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                //Log.i("app", "Response is: "+ response);
                try {
                    JSONObject jObject = new JSONObject(response);
                    Iterator<?> keys = jObject.keys();
                    Double distance = null;
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (jObject.get(key) instanceof JSONObject) {
                            JSONObject stop = jObject.getJSONObject(key);
                            double stopLong = stop.getDouble("stop_lon");
                            double stopLati = stop.getDouble("stop_lat");
                            double origin = longitude + latitude;
                            double stopLoc = stopLong + stopLati;
                            double locDiff = origin - stopLoc;
                            if(distance == null) {
                                distance = Math.abs(locDiff);
                            }
                            if (Math.abs(locDiff) < distance) {
                                distance = Math.abs(locDiff);
                                Pysakki.setStopId(key);
                                Pysakki.setStopLat(stopLati);
                                Pysakki.setStopLong(stopLong);
                                Pysakki.setStopName(stop.getString("stop_name"));
                            }
                           //Log.i("app", String.valueOf(locDiff) + stop.getString("stop_name"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String lahin = Pysakki.getStopName();
                Log.i("app", lahin);
            }
        });

    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
