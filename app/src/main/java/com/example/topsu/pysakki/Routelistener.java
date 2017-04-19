package com.example.topsu.pysakki;

import android.content.Context;
import android.location.Location;

import org.json.JSONObject;


/**
 * Created by Topsu on 19.4.2017.
 */

public interface Routelistener {

    void getRoute(JSONObject route);
}
