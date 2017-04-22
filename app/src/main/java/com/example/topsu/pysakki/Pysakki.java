package com.example.topsu.pysakki;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Topsu on 14.4.2017.
 */

public class Pysakki extends Application implements Comparable<Pysakki> {
    private Context context;
    private String stopId;
    private String stopName = null;

    private Float distance = null;

    private Location location;

    public void setStopId(String id){
        this.stopId = id;
    }

    public String getStopId(){
        return this.stopId;
    }


    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getStopName(){
        return stopName;
    }

    public Location getLocation() {
        return location;
    }


    @Override
    public int compareTo(@NonNull Pysakki o) {
        if(o.distance < this.distance) {
            return 1;
        } else if(o.distance > distance) {
            return -1;
        }
        return 0;
    }


}
