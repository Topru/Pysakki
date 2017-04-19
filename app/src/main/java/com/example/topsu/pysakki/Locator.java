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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Topsu on 18.4.2017.
 */

public class Locator extends AppCompatActivity {
    GoogleApiClient apiClient;

    Context context;

    private Location stopLocation;

    protected Location mLastLocation;


    Locator(GoogleApiClient apiClient, Context context) {
        this.context = context.getApplicationContext();
        this.apiClient = apiClient;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                apiClient);
        Log.i("lcoator", mLastLocation.toString());
    }

    public Pysakki getClosestPysakki(String json){
        Pysakki Pysakki = new Pysakki();
        //Log.i("app", "Response is: "+ response);
        try {
            JSONObject jObject = new JSONObject(json);
            Iterator<?> keys = jObject.keys();
            Double distance = null;
            while (keys.hasNext()) {

                String key = (String) keys.next();
                if (jObject.get(key) instanceof JSONObject) {
                    JSONObject stop = jObject.getJSONObject(key);
                    double stopLong = stop.getDouble("stop_lon");
                    double stopLati = stop.getDouble("stop_lat");

                    Location newStopLoc = new Location("newStoploc");
                    Log.i("Locator", "starting route request");

                    //todo: limit to 4 closest stops
                    Route.makeRouteRequest(context, mLastLocation, newStopLoc, "walking", new Routelistener() {
                        @Override
                        public void getRoute(JSONObject route) {
                            Log.i("Locator", route.toString());
                        }
                    });

                   /* if(distance == null) {
                        distance = Math.abs(locDiff);
                    }
                    //Log.i("app", stop.getString("stop_name") + Boolean.toString(Math.abs(locDiff) < distance) + Double.toString(distance));
                    if (Math.abs(locDiff) < distance) {
                        stopLocation = new Location("stoploc");
                        distance = Math.abs(locDiff);
                        stopLocation.setLatitude(stopLati);
                        stopLocation.setLongitude(stopLong);
                        Pysakki.setStopId(key);
                        Pysakki.setLocation(stopLocation);
                        Pysakki.setStopName(stop.getString("stop_name"));
                    }*/
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String lahin = Pysakki.getStopName();
        //Log.i("app", lahin);
        return Pysakki;
    }

    public Location getLocation(){
        return this.mLastLocation;
    }

}
