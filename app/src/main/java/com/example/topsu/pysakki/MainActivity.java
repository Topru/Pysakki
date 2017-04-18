package com.example.topsu.pysakki;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getLocation(View v) {
        // instantiate the location manager, note you will need to request permissions in your manifest
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // get the last know location from your location manager.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // now get the lat/lon from the location and do something with it.
        //nowDoSomethingWith(location.getLatitude(), location.getLongitude());
        final Pysakki Pysakki = new Pysakki();
        VolleyRequest.makeVolleyRequest(this, "http://data.foli.fi/gtfs/stops", new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                //Log.i("app", "Response is: "+ response);
                try {
                    JSONObject jObject = new JSONObject(response);
                    Iterator<?> keys = jObject.keys();
                    double distance = 9999;
                    String closest = "";
                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        if ( jObject.get(key) instanceof JSONObject ) {
                            JSONObject stop = jObject.getJSONObject(key);
                            double stopLong = stop.getDouble("stop_lon");
                            double stopLati = stop.getDouble("stop_lat");
                            double origin = Math.pow(location.getLongitude(), 2) + Math.pow(location.getLatitude(), 2);
                            double stopLoc = Math.pow(stopLong, 2) + Math.pow(stopLati, 2);
                            double locDiff = origin-stopLoc;
                            if(Math.abs(locDiff) < distance ) {
                                distance = Math.abs(locDiff);
                                Pysakki.setStopId(key);
                                Pysakki.setStopLat(stopLati);
                                Pysakki.setStopLong(stopLong);
                                Pysakki.setStopName(stop.getString("stop_name"));
                                closest = key;
                            }
                            Log.i("app", String.valueOf(distance) + " " + closest);
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

}
