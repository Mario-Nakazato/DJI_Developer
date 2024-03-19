package com.dev.coverpathplan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class GsdActivity extends AppCompatActivity {

    private TextView textAltitude, textGsdLarCm, textGsdAltCm, textGsdLar, textGsdAlt,
            textFootprintLar, textFootprintAlt, textImgLar, textImgAlt,
            textSensorLar, textSensorAlt, textFatorCorte, textE35mm, textDistanciaFocal,
            textOverlapLar, textOverlapAlt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsd);

        // Inicializar os TextViews
        initTextViews();

        // Atualizar os valores iniciais
        updateValues(null);

        // Adicionar listeners de texto
        addTextListeners();
    }

    private void initTextViews() {
        textAltitude = findViewById(R.id.altitude);
        textGsdLarCm = findViewById(R.id.gsdLarCm);
        textGsdAltCm = findViewById(R.id.gsdAltCm);
        textGsdLar = findViewById(R.id.gsdLar);
        textGsdAlt = findViewById(R.id.gsdAlt);
        textFootprintLar = findViewById(R.id.footprintLar);
        textFootprintAlt = findViewById(R.id.footprintAlt);
        textImgLar = findViewById(R.id.imgLar);
        textImgAlt = findViewById(R.id.imgAlt);
        textSensorLar = findViewById(R.id.sensorLar);
        textSensorAlt = findViewById(R.id.sensorAlt);
        textFatorCorte = findViewById(R.id.fatorCorte);
        textE35mm = findViewById(R.id.e35mm);
        textDistanciaFocal = findViewById(R.id.distanciaFocal);
        textOverlapLar = findViewById(R.id.overlapLar);
        textOverlapAlt = findViewById(R.id.overlapAlt);
    }

    private void updateValues(TextView excludedTextView) {
        CaptureArea.printGSD();
        if (textAltitude != excludedTextView) textAltitude.setText(String.valueOf(CaptureArea.getAltitude()));
        if (textGsdLarCm != excludedTextView) textGsdLarCm.setText(String.valueOf(CaptureArea.getGsdLarguraCm()));
        if (textGsdAltCm != excludedTextView) textGsdAltCm.setText(String.valueOf(CaptureArea.getGsdAlturaCm()));
        if (textGsdLar != excludedTextView) textGsdLar.setText(String.valueOf(CaptureArea.getGsdLargura()));
        if (textGsdAlt != excludedTextView) textGsdAlt.setText(String.valueOf(CaptureArea.getGsdAltura()));
        if (textFootprintLar != excludedTextView) textFootprintLar.setText(String.valueOf(CaptureArea.getFootprintLargura()));
        if (textFootprintAlt != excludedTextView) textFootprintAlt.setText(String.valueOf(CaptureArea.getFootprintAltura()));
        if (textImgLar != excludedTextView) textImgLar.setText(String.valueOf(CaptureArea.getImagemLargura()));
        if (textImgAlt != excludedTextView) textImgAlt.setText(String.valueOf(CaptureArea.getImagemAltura()));
        if (textSensorLar != excludedTextView) textSensorLar.setText(String.valueOf(CaptureArea.getSensorLargura()));
        if (textSensorAlt != excludedTextView) textSensorAlt.setText(String.valueOf(CaptureArea.getSensorAltura()));
        if (textFatorCorte != excludedTextView) textFatorCorte.setText(String.valueOf(CaptureArea.getFatorCorte()));
        if (textE35mm != excludedTextView) textE35mm.setText(String.valueOf(CaptureArea.getEquivante35mm()));
        if (textDistanciaFocal != excludedTextView) textDistanciaFocal.setText(String.valueOf(CaptureArea.getDistanciaFocal()));
        if (textOverlapLar != excludedTextView) textOverlapLar.setText(String.valueOf(CaptureArea.getOverlapLargura()));
        if (textOverlapAlt != excludedTextView) textOverlapAlt.setText(String.valueOf(CaptureArea.getOverlapAltura()));
    }

    private void addTextListeners() {
        textAltitude.addTextChangedListener(createTextWatcher(textAltitude, CaptureArea::getAltitude, CaptureArea::setAltitude));
        textGsdLarCm.addTextChangedListener(createTextWatcher(textGsdLarCm, CaptureArea::getGsdLarguraCm, CaptureArea::setGsdLarguraCm));
        textGsdAltCm.addTextChangedListener(createTextWatcher(textGsdAltCm, CaptureArea::getGsdAlturaCm, CaptureArea::setGsdAlturaCm));
        textGsdLar.addTextChangedListener(createTextWatcher(textGsdLar, CaptureArea::getGsdLargura, CaptureArea::setGsdLargura));
        textGsdAlt.addTextChangedListener(createTextWatcher(textGsdAlt, CaptureArea::getGsdAltura, CaptureArea::setGsdAltura));
        textFootprintLar.addTextChangedListener(createTextWatcher(textFootprintLar, CaptureArea::getFootprintLargura, CaptureArea::setFootprintLargura));
        textFootprintAlt.addTextChangedListener(createTextWatcher(textFootprintAlt, CaptureArea::getFootprintAltura, CaptureArea::setFootprintAltura));
        textImgLar.addTextChangedListener(createTextWatcher(textImgLar, CaptureArea::getImagemLargura, CaptureArea::setImagemLargura));
        textImgAlt.addTextChangedListener(createTextWatcher(textImgAlt, CaptureArea::getImagemAltura, CaptureArea::setImagemAltura));
        textSensorLar.addTextChangedListener(createTextWatcher(textSensorLar, CaptureArea::getSensorLargura, CaptureArea::setSensorLargura));
        textSensorAlt.addTextChangedListener(createTextWatcher(textSensorAlt, CaptureArea::getSensorAltura, CaptureArea::setSensorAltura));
        textFatorCorte.addTextChangedListener(createTextWatcher(textFatorCorte, CaptureArea::getFatorCorte, CaptureArea::setFatorCorte));
        textE35mm.addTextChangedListener(createTextWatcher(textE35mm, CaptureArea::getEquivante35mm, CaptureArea::setEquivante35mm));
        textDistanciaFocal.addTextChangedListener(createTextWatcher(textDistanciaFocal, CaptureArea::getDistanciaFocal, CaptureArea::setDistanciaFocal));
        textOverlapLar.addTextChangedListener(createTextWatcher(textOverlapLar, CaptureArea::getOverlapLargura, CaptureArea::setOverlapLargura));
        textOverlapAlt.addTextChangedListener(createTextWatcher(textOverlapAlt, CaptureArea::getOverlapAltura, CaptureArea::setOverlapAltura));
    }

    private TextWatcher createTextWatcher(TextView textView, Getter getter, Setter setter) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double changed = Double.parseDouble(textView.getText().toString());
                    double currentValue = getter.getValue();
                    if (currentValue != changed) {
                        setter.setValue(changed);
                        updateValues(textView);
                    }
                } catch (NumberFormatException ignored) {}
            }
        };
    }

    interface Getter {
        double getValue();
    }

    interface Setter {
        void setValue(double value);
    }
}