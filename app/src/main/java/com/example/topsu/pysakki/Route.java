package com.example.topsu.pysakki;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Topsu on 19.4.2017.
 */

public class Route {

    public static void makeRouteRequest(Context context, Location from, List destList, String mode, final Routelistener listener){
        String curLoc = String.valueOf(from.getLatitude()) + "," + String.valueOf(from.getLongitude());
        String routeApiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + curLoc + "&destinations=";
        //sort Pysakkis by distance
        Collections.sort(destList);
        //keep only 25 (max distances per request in googles api) closest Pysakkis by bee line
        destList.subList(25, destList.size()).clear();
        for (Iterator<Pysakki> i = destList.iterator(); i.hasNext();) {
            Pysakki item = i.next();
            Location loc = item.getLocation();
            Log.i("route", item.getStopName());
            routeApiUrl+=String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude()) + "%7C";
        }
        routeApiUrl+= "&mode=" + mode;
        Log.i("routeApiUrl", routeApiUrl);
        VolleyRequest.makeVolleyRequest(context, routeApiUrl, new VolleyResponseListener() {
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
    //get index of the route with smallest distance
    public static int indexOfMin(JSONArray jsonArr){
        Integer indexOfMin = 0;
        ArrayList<String> list = new ArrayList<String>();
        if (jsonArr != null) {
            int len = jsonArr.length();
            for (int i=0;i<len;i++){
                try {
                    JSONObject route = jsonArr.getJSONObject(i);
                    JSONObject distance = route.getJSONObject("distance");
                    list.add(distance.get("value").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Integer index = Integer.parseInt(list.get(0));
        int i = 0;
        while(i<list.size()){

            if (Integer.parseInt(list.get(i)) < index ){
                index = Integer.parseInt(list.get(i));
                indexOfMin = i;
            }
            i++;
        }
        Log.i("helper", list.toString());
        Log.i("helper", String.valueOf(indexOfMin));
        Log.i("helper", String.valueOf(list.get(indexOfMin)));
        return indexOfMin;
    }
}
