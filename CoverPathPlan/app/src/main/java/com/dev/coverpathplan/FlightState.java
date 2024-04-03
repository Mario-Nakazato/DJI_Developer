package com.dev.coverpathplan;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FlightState implements Cloneable {
    String currentDateTime;
    boolean areMotorsOn;
    boolean isFlying;
    double latitude;
    double longitude;
    double altitude;
    float positionX;
    float positionY;
    float positionZ;
    float takeoffLocationAltitude;
    double pitch;
    double roll;
    double yaw;
    float velocityX;
    float velocityY;
    float velocityZ;
    int flightTimeInSeconds;
    String flightMode;
    int satelliteCount;
    float ultrasonicHeight;
    int flightCount;
    String aircraftHeadDirection;

    FlightState() {
        currentDateTime = timeStamp2Date("dd/MM/yyyy HH:mm:ss.SSS");
        latitude = 181;
        longitude = 181;
    }

    public static String timeStamp2Date(String format) {

        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd-HH-mm-ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long time = System.currentTimeMillis();
        return sdf.format(new Date(time));
    }

    public static String convertingDoubleToHoursMinutesSecondsMilliseconds(long time) {
        time *= 1000;
        return convertingToHoursMinutesSecondsMilliseconds(time);
    }

    public static String convertingToHoursMinutesSecondsMilliseconds(long time) {
        // Convertendo para horas, minutos, segundos e milissegundos
        long Hours = time / (60 * 60 * 1000);
        long Minutes = (time % (60 * 60 * 1000)) / (60 * 1000);
        long Seconds = ((time % (60 * 60 * 1000)) % (60 * 1000)) / 1000;
        long Milliseconds = ((time % (60 * 60 * 1000)) % (60 * 1000)) % 1000;

        // Construindo a string
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