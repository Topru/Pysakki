package com.example.topsu.pysakki;

import android.util.Log;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Created by Topsu on 28.4.2017.
 */

public class Trip {
    private String tripId;

    private String arrivalTime;

    public String getTripId() {
        return tripId;
    }



    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Calendar getTimeAsCalendar() {
        Calendar cal = Calendar.getInstance();
        String[] splitTime = arrivalTime.split(Pattern.quote(":"));
        Integer hours = Integer.valueOf(splitTime[0]);
        Integer minutes = Integer.valueOf(splitTime[1]);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        return cal;
    }

}
