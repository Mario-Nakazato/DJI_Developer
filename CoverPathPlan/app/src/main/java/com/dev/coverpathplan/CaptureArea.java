package com.dev.coverpathplan;

import android.util.Log;

public class CaptureArea {
    // Parametros da câmera do Mavic Pro
    private static double altitude = 16; // m
    private static double imagemLargura = 4000; // px
    private static double imagemAltura = 3000; // px
    private static double sensorLargura = 6.17; // mm
    private static double sensorAltura = 4.55; // mm
    private static double sensorImagemLargura = 4000; // px
    private static double sensorImagemAltura = 3000; // px
    private static double fatorCorte = 5.64;
    private static double equivante35mm = 26; // mm
    private static double distanciaFocal = equivante35mm / fatorCorte; // mm
    private static double footprintLargura = altitude * (sensorLargura * imagemLargura / sensorImagemLargura) / distanciaFocal; // m
    private static double gsdLargura = footprintLargura / imagemLargura; // m/px
    private static double gsdLarguraCm = gsdLargura * 100; // cm/px
    private static double footprintAltura = altitude * (sensorAltura * imagemAltura / sensorImagemAltura) / distanciaFocal; // m
    private static double gsdAltura = footprintAltura / imagemAltura; // m/px
    private static double gsdAlturaCm = gsdAltura * 100; // cm/px
    private static double overlapLargura = 0.6; // %
    private static double overlapAltura = 0.6; // %

    static double getAltitude() {
        return altitude;
    }

    static double getImagemLargura() {
        return imagemLargura;
    }

    static double getImagemAltura() {
        return imagemAltura;
    }

    static double getSensorLargura() {
        return sensorLargura;
    }

    static double getSensorAltura() {
        return sensorAltura;
    }

    static double getSensorImagemLargura() {
        return sensorImagemLargura;
    }

    static double getSensorImagemAltura() {
        return sensorImagemAltura;
    }

    static double getFatorCorte() {
        return fatorCorte;
    }

    static double getEquivante35mm() {
        return equivante35mm;
    }

    static void setAltitude(double alt) {
        if (alt < 0)
            return;
        altitude = alt;
        calcFootprintLargura();
        calcFootprintAltura();
    }

    static void setImagemLargura(double imgLar) {
        if (imgLar < 0)
            return;
        imagemLargura = imgLar;
        calcFootprintLargura();
    }

    static void setImagemAltura(double imgAlt) {
        if (imgAlt < 0)
            return;
        imagemAltura = imgAlt;
        calcFootprintAltura();
    }

    static void setSensorLargura(double sensorLar) {
        if (sensorLar < 0)
            return;
        sensorLargura = sensorLar;
        calcFootprintLargura();
    }

    static void setSensorAltura(double sensorAlt) {
        if (sensorAlt < 0)
            return;
        sensorAltura = sensorAlt;
        calcFootprintAltura();
    }

    static void setSensorImagemLargura(double sensorImagemLar) {
        if (sensorImagemLar < 0)
            return;
        sensorImagemLargura = sensorImagemLar;
        calcFootprintLargura();
    }

    static void setSensorImagemAltura(double sensorImagemAlt) {
        if (sensorImagemAlt < 0)
            return;
        sensorImagemAltura = sensorImagemAlt;
        calcFootprintAltura();
    }

    static void setFatorCorte(double fCorte) {
        if (fCorte < 0)
            return;
        fatorCorte = fCorte;
        calcDistanciaFocal();
    }

    static void setEquivante35mm(double e35mm) {
        if (e35mm < 0)
            return;
        equivante35mm = e35mm;
        calcDistanciaFocal();
    }

    static double getDistanciaFocal() {
        return distanciaFocal;
    }

    static void setDistanciaFocal(double disFoc) {
        if (disFoc < 0)
            return;
        distanciaFocal = disFoc;
        equivante35mm = 0;
        fatorCorte = 0;
        calcFootprintLargura();
        calcFootprintAltura();
    }

    private static void calcDistanciaFocal() {
        distanciaFocal = fatorCorte != 0 ? equivante35mm / fatorCorte : 0;
        calcFootprintLargura();
        calcFootprintAltura();
    }

    static double getFootprintLargura() {
        return footprintLargura;
    }

    static void setFootprintLargura(double footprintLar) {
        if (footprintLar < 0)
            return;
        footprintLargura = footprintLar;
        setAltitude(sensorLargura != 0 ? footprintLargura * distanciaFocal / (sensorLargura * imagemLargura / sensorImagemLargura) : 0);
    }

    private static void calcFootprintLargura() {
        footprintLargura = distanciaFocal != 0 ? altitude * (sensorLargura * imagemLargura / sensorImagemLargura) / distanciaFocal : 0;
        calcGsdLargura();
    }

    static double getGsdLargura() {
        return gsdLargura;
    }

    static void setGsdLargura(double gsdLar) {
        if (gsdLar < 0)
            return;
        gsdLargura = gsdLar;
        setFootprintLargura(gsdLargura * imagemLargura);
    }

    private static void calcGsdLargura() {
        gsdLargura = imagemLargura != 0 ? footprintLargura / imagemLargura : 0;
        calcGsdLarguraCm();
    }

    static double getGsdLarguraCm() {
        return gsdLarguraCm;
    }

    static void setGsdLarguraCm(double gsdLarCm) {
        if (gsdLarCm < 0)
            return;
        gsdLarguraCm = gsdLarCm;
        setGsdLargura(gsdLarguraCm / 100);
    }

    private static void calcGsdLarguraCm() {
        gsdLarguraCm = gsdLargura * 100;
    }

    static double getFootprintAltura() {
        return footprintAltura;
    }

    static void setFootprintAltura(double footprintAlt) {
        if (footprintAlt < 0)
            return;
        footprintAltura = footprintAlt;
        setAltitude(sensorAltura != 0 ? footprintAltura * distanciaFocal / (sensorAltura * imagemAltura / sensorImagemAltura) : 0);
    }

    private static void calcFootprintAltura() {
        footprintAltura = distanciaFocal != 0 ? altitude * (sensorAltura * imagemAltura / sensorImagemAltura) / distanciaFocal : 0;
        calcGsdAltura();
    }

    static double getGsdAltura() {
        return gsdAltura;
    }

    static void setGsdAltura(double gsdAlt) {
        if (gsdAlt < 0)
            return;
        gsdAltura = gsdAlt;
        setFootprintAltura(gsdAltura * imagemAltura);
    }

    private static void calcGsdAltura() {
        gsdAltura = imagemAltura != 0 ? footprintAltura / imagemAltura : 0;
        calcGsdAlturaCm();
    }

    static double getGsdAlturaCm() {
        return gsdAlturaCm;
    }

    static void setGsdAlturaCm(double gsdAltCm) {
        if (gsdAltCm < 0)
            return;
        gsdAlturaCm = gsdAltCm;
        setGsdAltura(gsdAlturaCm / 100);
    }

    private static void calcGsdAlturaCm() {
        gsdAlturaCm = gsdAltura * 100;
    }

    static double getOverlapLargura() {
        return overlapLargura;
    }

    static double getOverlapAltura() {
        return overlapAltura;
    }

    static void setOverlapLargura(double overlapLar) {
        if (overlapLar >= 1)
            return;
        overlapLargura = overlapLar;
    }

    static void setOverlapAltura(double overlapAlt) {
        if (overlapAlt >= 1)
            return;
        overlapAltura = overlapAlt;
    }

    static void printGSD() {
        Log.v("GSD", "Altitude: " + altitude + " m");
        Log.v("GSD", "GSD Largura em cm: " + gsdLarguraCm + " cm/px");
        Log.v("GSD", "GSD Altura em cm: " + gsdAlturaCm + " cm/px");
        Log.v("GSD", "GSD Largura: " + gsdLargura + " m/px");
        Log.v("GSD", "GSD Altura: " + gsdAltura + " m/px");
        Log.v("GSD", "Footprint Largura: " + footprintLargura + " m");
        Log.v("GSD", "Footprint Altura: " + footprintAltura + " m");
        Log.v("GSD", "Imagem Largura: " + imagemLargura + " px");
        Log.v("GSD", "Imagem Altura: " + imagemAltura + " px");
        Log.v("GSD", "Sensor Largura: " + sensorLargura + " mm");
        Log.v("GSD", "Sensor Altura: " + sensorAltura + " mm");
        Log.v("GSD", "Sensor Imagem Largura: " + sensorImagemLargura + " px");
        Log.v("GSD", "Sensor Imagem Altura: " + sensorImagemAltura + " px");
        Log.v("GSD", "Fator de Corte: " + fatorCorte);
        Log.v("GSD", "Equivalente 35mm: " + equivante35mm + "mm");
        Log.v("GSD", "Distância Focal: " + distanciaFocal + " mm");
        Log.v("GSD", "Overlap Largura: " + overlapLargura * 100 + " %");
        Log.v("GSD", "Overlap Altura: " + overlapAltura * 100 + " %");
    }
}