package com.dev.coverpathplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {

    private Button bMap;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, // Localização fina
            Manifest.permission.ACCESS_COARSE_LOCATION, // Localização aproximada
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_PERMISSION_CODE = 8664;
    private TextView mVersionTv;
    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
            //Listens to the SDK registration result
            @Override
            public void onRegister(DJIError error) {
                if (error == DJISDKError.REGISTRATION_SUCCESS) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Sucesso no registro do SDK");
                        }
                    });
                    DJISDKManager.getInstance().startConnectionToProduct();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Falha no registro do SDK, verifique se a rede está disponível");
                        }
                    });
                }
            }

            @Override
            public void onProductDisconnect() {
            }

            @Override
            public void onProductConnect(BaseProduct baseProduct) {
            }

            @Override
            public void onProductChanged(BaseProduct baseProduct) {
            }

            @Override
            public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
            }

            @Override
            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {
            }

            @Override
            public void onDatabaseDownloadProgress(long l, long l1) {
            }
        };

        // Verifica e solicita permissões se necessário
        checkAndRequestPermissions();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissões concedidas
                checkAndRequestPermissions();
                showToast("Permissões concedidas");
            } else {
                // Permissões negadas
                showToast("Permissões negadas");
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
            DJISDKManager.getInstance().registerApp(getApplicationContext(), mDJISDKManagerCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((Activity) this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void initUI() {
        bMap = findViewById(R.id.button);
        bMap.setEnabled(false);

        bMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int status = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
                if (status != ConnectionResult.SUCCESS) {
                    googleApiAvailability.getErrorDialog(MainActivity.this, status, 0).show();
                    showToast("Não é possível executar sem o Google Play, verifique!");
                } else {
                    openAoiActivity();
                }
            }
        });

        mVersionTv = (TextView) findViewById(R.id.textDJISDKVersion);
        mVersionTv.setText("Versão DJI SDK: " + DJISDKManager.getInstance().getSDKVersion());
    }

    private void openAoiActivity() {
        Intent intent = new Intent(MainActivity.this, AoiActivity.class);
        startActivity(intent);
    }

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}