package com.dev.coverpathplan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class GsdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsd);

        CaptureArea.printGSD();
        TextView textAltitude = findViewById(R.id.altitude);
        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
        TextView textGsdLarCm = findViewById(R.id.gsdLarCm);
        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
        TextView textGsdAltCm = findViewById(R.id.gsdAltCm);
        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
        TextView textGsdLar = findViewById(R.id.gsdLar);
        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
        TextView textGsdAlt = findViewById(R.id.gsdAlt);
        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
        TextView textFootprintLar = findViewById(R.id.footprintLar);
        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
        TextView textFootprintAlt = findViewById(R.id.footprintAlt);
        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
        TextView textImgLar = findViewById(R.id.imgLar);
        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
        TextView textImgAlt = findViewById(R.id.imgAlt);
        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
        TextView textSensorLar = findViewById(R.id.sensorLar);
        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
        TextView textSensorAlt = findViewById(R.id.sensorAlt);
        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
        TextView textFatorCorte = findViewById(R.id.fatorCorte);
        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
        TextView textE35mm = findViewById(R.id.e35mm);
        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
        TextView textDistanciaFocal = findViewById(R.id.distanciaFocal);
        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));

        textAltitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textAltitude.getText().toString());
                    if (CaptureArea.getAltitude() != changed) {
                        CaptureArea.setAltitude(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textGsdLarCm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textGsdLarCm.getText().toString());
                    if (CaptureArea.getGsdLarguraCm() != changed) {
                        CaptureArea.setGsdLarguraCm(changed);
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textGsdAltCm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textGsdAltCm.getText().toString());
                    if (CaptureArea.getGsdAlturaCm() != changed) {
                        CaptureArea.setGsdAlturaCm(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textGsdLar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textGsdLar.getText().toString());
                    if (CaptureArea.getGsdLargura() != changed) {
                        CaptureArea.setGsdLargura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textGsdAlt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textGsdAlt.getText().toString());
                    if (CaptureArea.getGsdAltura() != changed) {
                        CaptureArea.setGsdAltura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textFootprintLar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textFootprintLar.getText().toString());
                    if (CaptureArea.getFootprintLargura() != changed) {
                        CaptureArea.setFootprintLargura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textFootprintAlt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textFootprintAlt.getText().toString());
                    if (CaptureArea.getFootprintAltura() != changed) {
                        CaptureArea.setFootprintAltura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textImgLar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textImgLar.getText().toString());
                    if (CaptureArea.getImagemLargura() != changed) {
                        CaptureArea.setImagemLargura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textImgAlt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textImgAlt.getText().toString());
                    if (CaptureArea.getImagemAltura() != changed) {
                        CaptureArea.setImagemAltura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textSensorLar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textSensorLar.getText().toString());
                    if (CaptureArea.getSensorLargura() != changed) {
                        CaptureArea.setSensorLargura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textSensorAlt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textSensorAlt.getText().toString());
                    if (CaptureArea.getSensorAltura() != changed) {
                        CaptureArea.setSensorAltura(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textFatorCorte.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textFatorCorte.getText().toString());
                    if (CaptureArea.getFatorCorte() != changed) {
                        CaptureArea.setFatorCorte(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textE35mm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textE35mm.getText().toString());
                    if (CaptureArea.getEquivante35mm() != changed) {
                        CaptureArea.setEquivante35mm(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                        textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
                    }
                } catch (Exception e) {
                }
            }
        });

        textDistanciaFocal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.valueOf(textDistanciaFocal.getText().toString());
                    if (CaptureArea.getDistanciaFocal() != changed) {
                        CaptureArea.setDistanciaFocal(changed);
                        textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
                        textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
                        textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
                        textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
                        textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
                        textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
                        textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
                        textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
                        textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
                        textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
                        textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
                        textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
                        textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
                    }
                } catch (Exception e) {
                }
            }
        });

    }
}