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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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

    public void getClosestPysakki(String json, final PysakkiListener listener){
        Integer closestRouteIndex = null;
        //Log.i("app", "Response is: "+ response);
        final List<Pysakki> pysakkiList = new ArrayList();
        try {
            JSONObject jObject = new JSONObject(json);
            Iterator<?> keys = jObject.keys();
            Float distance = null;
            String currentLoc = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();

            while (keys.hasNext()) {

                String key = (String) keys.next();
                if (jObject.get(key) instanceof JSONObject) {
                    Pysakki Pysakki = new Pysakki();
                    JSONObject stop = jObject.getJSONObject(key);
                    double stopLong = stop.getDouble("stop_lon");
                    double stopLati = stop.getDouble("stop_lat");
                    String stopLoc = Double.valueOf(stopLati) + "," + Double.valueOf(stopLong);
                    stopLocation = new Location("stoploc");
                    stopLocation.setLatitude(stopLati);
                    stopLocation.setLongitude(stopLong);
                    float stopDistance = mLastLocation.distanceTo(stopLocation);
                    Pysakki.setLocation(stopLocation);
                    Pysakki.setStopId(key);
                    Pysakki.setStopName(stop.getString("stop_name"));
                    Pysakki.setDistance(stopDistance);

                    pysakkiList.add(Pysakki);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //String lahin = Pysakki.getStopName();
        //Log.i("app", lahin);
        Route.makeRouteRequest(context, mLastLocation, pysakkiList, "walking", new Routelistener() {
            @Override
            public void getRoute(JSONObject route) {
                try {
                    JSONArray routeArray = route.getJSONArray("rows");
                    JSONObject routesObject = routeArray.getJSONObject(0);
                    JSONArray routes = routesObject.getJSONArray("elements");
                    Log.i("loc", routes.toString());
                    Collections.sort(pysakkiList);
                    listener.getStop(pysakkiList.get(Route.indexOfMin(routes)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Location getLocation(){
        return this.mLastLocation;
    }

}
