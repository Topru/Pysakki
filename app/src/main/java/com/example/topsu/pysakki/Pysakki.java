package com.example.topsu.pysakki;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Topsu on 14.4.2017.
 */

public class Pysakki extends Application {
    private Context context;
    private String stopId;
    private String stopName = null;

    private Location location;




    public void setStopId(String id){
        this.stopId = id;
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
}
