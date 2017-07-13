package com.example.topsu.pysakki;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TimePickerDialog.OnTimeSetListener {

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;

    protected Locator locator;
    protected Location curLoc;
    protected Boolean mRequestingLocationUpdates;

    protected Calendar cal = Calendar.getInstance();

    protected Button timeButton;

    protected Context context = this;

    protected LatLng destination;

    private static final int mapResultCode = 0;

    protected JSONObject stopList;

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
        createLocationRequest();
        buildLocationSettingsRequest();
        cal.setTime(new Date());
        timeButton = (Button)this.findViewById(R.id.timepicker);
        String hourString;
        if (cal.get(Calendar.HOUR_OF_DAY) < 10) {
            hourString = "0" + cal.get(Calendar.HOUR_OF_DAY);
        }
        else {
            hourString = "" + cal.get(Calendar.HOUR_OF_DAY);
        }
        String minuteString;
        if (cal.get(Calendar.MINUTE) < 10) {
            minuteString = "0" + cal.get(Calendar.MINUTE);
        }
        else {
            minuteString = "" + cal.get(Calendar.MINUTE);
        }
        timeButton.setText(hourString + ":" + minuteString);
        Log.i("selectedHour", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
        Log.i("selectedMin", Integer.toString(cal.get(Calendar.MINUTE)));

    }
    public void setDestination(View view) {
        Location location = new Locator(mGoogleApiClient, this).getLocation();
        curLoc = location;
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lat", location.getLatitude());
        intent.putExtra("lon", location.getLongitude());
        startActivityForResult(intent, mapResultCode);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == mapResultCode) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getParcelableExtra("bundle");
                destination = bundle.getParcelable("destination");
                Log.i("destination", destination.toString());
            }
        }

    }



    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        FragmentManager fm = getFragmentManager();
        newFragment.show(fm, "timePicker");
    }

    public void makePysakkiRequest(View v) {
        VolleyRequest.makeVolleyRequest(this, "http://data.foli.fi/gtfs/stops", new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                try {
                    stopList = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handlePysakki(response);
            }
        });
    }

    public void handlePysakki(String response) {
        final Locator locator = new Locator(mGoogleApiClient, this);
        final Location destLoc = new Location("dummyprovider");
        destLoc.setLongitude(destination.longitude);
        destLoc.setLatitude(destination.latitude);
        locator.getClosestPysakki(response, destLoc, new PysakkiListener() {
            @Override
            public void getStop(Pysakki pysakki) {
                final Pysakki destPysakki = pysakki;
                Log.i("destinationStop", destPysakki.getStopName());
                Log.i("destinationId", destPysakki.getStopId());
                VolleyRequest.makeVolleyRequest(context, "http://data.foli.fi/gtfs/calendar_dates", new VolleyResponseListener() {
                    @Override
                    public void getResult(String response) {
                        TextView finishStopView = (TextView)findViewById(R.id.finishStopView);
                        finishStopView.setText(destPysakki.getStopName());
                        handleStopTimes(response, destPysakki);
                    }
                });
            }
        });
    }

    protected void handleStopTimes(String response, Pysakki destPysakki){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = dateFormat.format(cal.getTime());
        final List<String> serviceList = new ArrayList();

        try {
            JSONObject jObject = new JSONObject(response);

            Iterator<?> keys = jObject.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (jObject.get(key) instanceof JSONArray) {
                    JSONArray jArray = jObject.getJSONArray(key);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject servObj = jArray.getJSONObject(i);
                        if(servObj.getString("date").equals(formattedDate)){
                            serviceList.add(key);
                        }

                    }
                }
            }
            Log.i("servicelist", serviceList.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.makeVolleyRequest(context, "http://data.foli.fi/gtfs/stop_times/stop/" + destPysakki.getStopId(), new VolleyResponseListener() {
            @Override
            public void getResult(String response){
                final List<Trip> tripList = new ArrayList();
                try {
                    JSONArray jArray = new JSONArray(response);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject row = jArray.getJSONObject(i);
                        Trip trip = new Trip();
                        String tripId = row.getString("trip_id");
                        String arrivalTime = row.getString("arrival_time");
                        trip.setTripId(tripId);
                        trip.setArrivalTime(arrivalTime);
                        tripList.add(trip);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parseTrips(tripList, serviceList);
            }
        });
    }

    private void parseTrips(final List<Trip> tripList, final List<String> serviceList) {
        Long arMsTime = cal.getTimeInMillis();
        Long timeDif = null;
        Integer closestHigherIndex = 0;
        for (int i = 0; i < tripList.size(); i++) {
            Long tripMsTime = tripList.get(i).getTimeAsCalendar().getTimeInMillis();
            if (tripMsTime >= arMsTime) {
                Long newTimedif = tripMsTime - arMsTime;
                if (timeDif == null) {
                    timeDif = newTimedif;
                    closestHigherIndex = i;
                }
                if (newTimedif < timeDif) {
                    timeDif = newTimedif;
                    closestHigherIndex = i;
                    Log.i("timedif", timeDif.toString());
                }
            }
        }
        String tripId = tripList.get(closestHigherIndex).getTripId();
        final int index = closestHigherIndex;
        VolleyRequest.makeVolleyRequest(context, "http://data.foli.fi/gtfs/trips/trip/" + tripId, new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                try {
                    JSONArray jArray = new JSONArray(response);
                    JSONObject trip = jArray.getJSONObject(0);
                    String serviceId = trip.getString("service_id");
                    if(serviceList.contains(serviceId)) {
                        Log.i("tripfinder", "got it");
                        getStartStop(tripList.get(index).getTripId());
                    } else {
                        Log.i("tripfinder", "nope");
                        tripList.remove(index);
                        parseTrips(tripList, serviceList);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        Log.i("tripId", tripList.get(closestHigherIndex).getTripId());
    }

    private void getStartStop(final String tripId) {
        VolleyRequest.makeVolleyRequest(context, "http://data.foli.fi/gtfs/stop_times/trip/" + tripId, new VolleyResponseListener() {
            @Override
            public void getResult(String response) {
                try {
                    final JSONArray tripStopsArray = new JSONArray(response);
                    final JSONObject stopObject = new JSONObject();
                    List<String> tripStopIdList = new ArrayList<>();
                    for (int i = 0; i < tripStopsArray.length(); i++) {
                        JSONObject stop = tripStopsArray.getJSONObject(i);
                        tripStopIdList.add(stop.getString("stop_id"));
                        Log.i("stopId", stop.getString("stop_id"));
                    }
                    Iterator<?> keys = stopList.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (stopList.get(key) instanceof JSONObject) {
                            if(tripStopIdList.toString().matches(".*\\b" + key + "\\b.*")) {
                                Log.i("matcher", "match!");
                                stopObject.accumulate(key, stopList.get(key));
                            }
                        }
                    }
                    Log.i("stopIdList", tripStopIdList.toString());
                    Log.i("stopObject", stopObject.toString());
                    final Locator locator = new Locator(mGoogleApiClient, context);
                    locator.getClosestPysakki(stopObject.toString(), curLoc, new PysakkiListener(){

                        @Override
                        public void getStop(Pysakki pysakki) {
                            Log.i("startstop", pysakki.getStopName());
                            Log.i("loc", String.valueOf(locator.getLocation().getLatitude()));
                            String goTime = null;
                            for (int i = 0; i < tripStopsArray.length(); i++) {
                                try {
                                    JSONObject stop = tripStopsArray.getJSONObject(i);
                                    Log.i("asd1", stop.getString("stop_id") + " " + pysakki.getStopId());
                                    if (stop.getString("stop_id").equals(pysakki.getStopId())) {
                                        Log.i("dafq", "asdasd");
                                        goTime = stop.getString("arrival_time");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            TextView startStopView = (TextView)findViewById(R.id.startStopView);
                            startStopView.setText(pysakki.getStopName() + " " + goTime);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(5000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(5000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void startLocationUpdates() {
        Log.i("locationupdates", "locupdates started");
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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i("locationupdates", location.toString());
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (true) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeButton.setText(String.format("%02d:%02d", hourOfDay, minute));
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        Log.i("time", cal.getTime().toString());
    }

}
