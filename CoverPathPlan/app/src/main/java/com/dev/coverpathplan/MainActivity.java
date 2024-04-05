package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

@SuppressLint("SetTextI18n")
@SuppressWarnings("Convert2Lambda")
public class MainActivity extends AppCompatActivity {

    private Button bMap, bDebug;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, // Localização fina
            Manifest.permission.ACCESS_COARSE_LOCATION, // Localização aproximada
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_PERMISSION_CODE = 8664;
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private TextView mTextConnectionStatus;
    private TextView mTextProduct;
    private TextView mVersionTv;
    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback;
    private static BaseProduct mProduct;
    private static WaypointMissionOperator mMissionOperator;
    private static FirebaseDatabase database;
    private Handler mHandler;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        mHandler = new Handler(Looper.getMainLooper());

        initUI();

        mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
            //Listens to the SDK registration result
            @Override
            public void onRegister(DJIError error) {
                Handler handler = new Handler(Looper.getMainLooper());
                if (error == DJISDKError.REGISTRATION_SUCCESS) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Sucesso no registro do SDK");
                        }
                    });
                    DJISDKManager.getInstance().startConnectionToProduct();
                } else {
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
                notifyStatusChange();
            }

            @Override
            public void onProductConnect(BaseProduct baseProduct) {
                notifyStatusChange();
            }

            @Override
            public void onProductChanged(BaseProduct baseProduct) {
                notifyStatusChange();
            }

            @Override
            public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                          BaseComponent newComponent) {
                if (newComponent != null) {
                    newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                        @Override
                        public void onConnectivityChange(boolean isConnected) {
                            notifyStatusChange();
                        }
                    });
                }
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
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            ActivityCompat.requestPermissions(this,
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

        bDebug = findViewById(R.id.debug);

        bDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bMap.setEnabled(true);
            }
        });

        mTextConnectionStatus = findViewById(R.id.text_connection_status);
        mTextProduct = findViewById(R.id.text_product_info);
        mVersionTv = findViewById(R.id.textDJISDKVersion);
        mVersionTv.setText("Versão DJI SDK: " + DJISDKManager.getInstance().getSDKVersion());
    }

    private void openAoiActivity() {
        Intent intent = new Intent(MainActivity.this, AoiActivity.class);
        startActivity(intent);
    }

    private void refreshSDKRelativeUI() {
        getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            String str = mProduct instanceof Aircraft ? "Aeronave DJI" : "Dispositivo portátil DJI";
            mTextConnectionStatus.setText("Status: " + str + " conectado");

            if (null != mProduct.getModel()) {
                mTextProduct.setText(mProduct.getModel().getDisplayName());
            } else {
                mTextProduct.setText("Modelo");
            }
            bMap.setEnabled(true);
        } else {
            if (mProduct instanceof Aircraft) {
                Aircraft aircraft = (Aircraft) mProduct;
                if (aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                    // The product is not connected, but the remote controller is connected
                    mTextConnectionStatus.setText("Status: apenas RC conectado");
                    mTextProduct.setText("Controle remoto");
                }
            } else {
                mTextConnectionStatus.setText("Status: Desconectado");
                mTextProduct.setText("Modelo");
                bMap.setEnabled(false);
            }
        }
    }

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    public static synchronized WaypointMissionOperator getMissionOperatorInstance() {
        if (mMissionOperator == null) {
            if (DJISDKManager.getInstance().getMissionControl() != null) {
                mMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return mMissionOperator;
    }

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true); // Permite funcionalidade offline persiste armazenamento local
            database.goOnline(); // (Re)conecta ao banco de dados sincroniza em tempo real
        }
        return database;
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            getApplicationContext().sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}