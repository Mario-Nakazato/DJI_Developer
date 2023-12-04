package com.dev.coverpathplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button bMap;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, // Localização fina
            Manifest.permission.ACCESS_COARSE_LOCATION, // Localização aproximada
    };
    private static final int REQUEST_PERMISSION_CODE = 8664;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bMap = findViewById(R.id.button);
        bMap.setEnabled(false);

        bMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAoiActivity();
            }
        });

        // Verifica e solicita permissões se necessário
        checkAndRequestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissões concedidas
                checkAndRequestPermissions();
                Toast.makeText(this, "Permissões concedidas", Toast.LENGTH_SHORT).show();
            } else {
                // Permissões negadas
                Toast.makeText(this, "Permissões negadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndRequestPermissions() {
        // Verificar permissões
        List<String> missingPermission = new ArrayList<>();
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Solicitar permissões ausentes
        if (missingPermission.isEmpty()) {
            bMap.setEnabled(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((Activity) this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void openAoiActivity() {
        Intent intent = new Intent(MainActivity.this, AoiActivity.class);
        startActivity(intent);
    }
}