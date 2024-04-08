package com.dev.coverpathplan;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FlightState implements Cloneable {
    String currentDateTime;
    boolean areMotorsOn = false;
    boolean isFlying = false;
    double latitude;
    double longitude;
    double altitude = 0;
    float positionX = 0;
    float positionY = 0;
    float positionZ = 0;
    float takeoffLocationAltitude = 0;
    double pitch = 0;
    double roll = 0;
    double yaw = 0;
    float velocityX = 0;
    float velocityY = 0;
    float velocityZ = 0;
    int flightTimeInSeconds = 0;
    String flightMode = "";
    int satelliteCount = 0;
    float ultrasonicHeight = 0;
    int flightCount = 0;
    String aircraftHeadDirection = "";

    FlightState() {
        currentDateTime = timeStamp2Date("dd/MM/yyyy HH:mm:ss.SSS");
        latitude = 181;
        longitude = 181;
    }

    public static String timeStamp2Date(String format) {

        if (format == null || format.isEmpty())
            format = "yyyy-MM-dd-HH-mm-ss";

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long time = System.currentTimeMillis();

        return sdf.format(new Date(time));
    }

    public static String convertingDoubleToHoursMinutesSecondsMilliseconds(long time) {
        time *= 1000;
        return convertingToHoursMinutesSecondsMilliseconds(time);
    }

    public static String convertingToHoursMinutesSecondsMilliseconds(long time) {
        long Hours = time / (60 * 60 * 1000);
        long Minutes = (time % (60 * 60 * 1000)) / (60 * 1000);
        long Seconds = ((time % (60 * 60 * 1000)) % (60 * 1000)) / 1000;
        long Milliseconds = ((time % (60 * 60 * 1000)) % (60 * 1000)) % 1000;
        String timeFormat = String.format("%02d:%02d:%02d.%03d", Hours, Minutes, Seconds, Milliseconds);

        return timeFormat;
    }

    public static String calculateElapsedTime(String initialTime, String finalTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
            Date dataInicial = sdf.parse(initialTime);
            Date dataFinal = sdf.parse(finalTime);
            long diferenca = dataFinal.getTime() - dataInicial.getTime();

            return convertingToHoursMinutesSecondsMilliseconds(diferenca);
        } catch (Exception e) {
            e.printStackTrace();
            return "HH:mm:ss.SSS";
        }
    }

    @Override
    public FlightState clone() {
        try {
            return (FlightState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}