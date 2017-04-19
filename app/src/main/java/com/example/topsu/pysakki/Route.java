package com.example.topsu.pysakki;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Topsu on 19.4.2017.
 */

public class Route {

    public static void makeRouteRequest(Context context, Location from, Location dest, String mode, final Routelistener listener){
        String fromLoc = String.valueOf(from.getLongitude()) + "," + String.valueOf(from.getLatitude());
        String destLoc = String.valueOf(dest.getLongitude()) + "," + String.valueOf(dest.getLatitude());;
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + fromLoc + "&destination=" + destLoc + "&sensor=false&units=metric&mode=" + mode;
        VolleyRequest.makeVolleyRequest(context, url, new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                JSONObject jsonRoute = null;
                try {
                    jsonRoute = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.getRoute(jsonRoute);
            }
        });

    }
}
