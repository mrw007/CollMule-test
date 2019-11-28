package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        t1 = (TextView) findViewById(R.id.res);
        t2 = (TextView) findViewById(R.id.tit);
        t3 = (TextView) findViewById(R.id.stit);
        t4 = (TextView) findViewById(R.id.stat);
        dataList = new ArrayList<>();
        bleHandler = new Handler();
        cmHandler = new Handler();
        getPermWrapper();
        btDeviceList = new HashMap<>();
        cmr = new ContextManagerRepository(this);
        cmr.execute();

        try{
            senCoord = cmr.get();
        } catch (Exception e){
            Log.i(TAG, "it is impossible to get coordinates");
        }

        d = new ArrayList<>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //scheduleAlarm();
    }

    TextView t1;
    TextView t2;
    TextView t3;
    TextView t4;
    static Context context;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bleSc;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    public static HashMap<String, List> btDeviceList;
    private Handler bleHandler;
    boolean bleScanning;
    //Tracker gpsTracker;
    private List uuid;

    static ArrayList<String> address;
    static ArrayList<HashMap<String, String>> senCoord;
    ContextManagerRepository cmr;
    private Handler cmHandler;
    ArrayList<String> d;

    private final static String TAG = MainActivity.class.getSimpleName();
    private boolean mConnected = false;
    private BLEConnection bleConnection;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleConnection = ((BLEConnection.LocalBinder) service).getService();
            if (!bleConnection.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            /*for (int i=0; i<address.size(); i++) {
                mBluetoothLeService.connect(address.get(i));

            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleConnection = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    public ArrayList<HashMap<String, String>> dataList;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            HashMap<String, String> dataMap = new HashMap<>();
            final String action = intent.getAction();
            if (BLEConnection.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
                //invalidateOptionsMenu();
            } else if (BLEConnection.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("disconnected");
                //invalidateOptionsMenu();
            } else if (BLEConnection.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAG, "services discovered");
            } else if (BLEConnection.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.hasExtra("Temperature:")){
                    String addre = intent.getStringExtra("Address");
                    dataMap.put("Address", addre);
                    String temp = intent.getStringExtra("Temperature:");
                    dataMap.put("Temperature", temp);
                    t1.append("\nTemperature: "+temp);
                } else if (intent.hasExtra("Humidity:")){
                    String hum = intent.getStringExtra("Humidity:");
                    dataMap.put("Humidity: ", hum);
                    t1.append("\nHumidity: "+hum);
                } else if (intent.hasExtra("PM:")){
                    String pm = intent.getStringExtra("PM:");
                    dataMap.put("PM Concentration: ", pm);
                    t1.append("\nPM Concentration: "+pm);
                }
                dataList.add(dataMap);
                Log.i(TAG, "charachteristics");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConnection.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConnection.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConnection.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConnection.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    private void updateConnectionState(final String state) {
        t3.setText("Status:");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t4.setText(""+state);
            }
        });
    }

    public void onButtonClick(View v){
        if (v.getId()==R.id.scan) {
            t1.setText("");
            t2.setText("Discovered devices:");
            scanLeDevice(true);
        }
    }

    public void onButConnectClick(View v){
        t2.setText("Collected data:");
        t1.setText("");

        Intent gattServiceIntent = new Intent(this, BLEConnection.class);
        gattServiceIntent.putExtra(BLEConnection.COMMAND_KEY, BLEConnection.COMMAND_START_CONNECTION);
        gattServiceIntent.putExtra(BLEConnection.EXTRA_DATA, address);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(gattServiceIntent);

    }

    CollMule m;
    public void onButSendClick(View v){

        t1.setText("");
        t2.setText("Ranked devices:");
        exAlg(true);

    }

    public void exAlg(final boolean enable){
        if (enable){
            cmHandler.post(new Runnable() {
                @Override
                public void run() {
                    long timeStart = System.nanoTime();
                    Log.i("Time Started", ""+timeStart);
                    m = new CollMule();
                    address = m.orderedDev();
                    int place = 1;
                    for (int i=address.size()-1; i > -1; i--){
                        t1.append("\n"+place+ " - " + address.get(i));
                        place++;
                    }
                    long timeElapsed = System.nanoTime()-timeStart;
                    Log.i("Time Elapsed", ""+timeElapsed);
                }
            });
        } else {
            Log.i(TAG, "unsuccessful attempt");
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            bleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleScanning = false;
                    bleSc.stopScan(scanCallback);
                }
            }, SCAN_PERIOD);
            btDeviceList.clear();
            bleScanning = true;
            bleSc.startScan(filters, settings, scanCallback);
        } else {
            bleSc.stopScan(scanCallback);
            Log.i("check", "scan");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for Bluetooth support and then check to make sure it is turned on
        // If it isn't request to turn it on
        // List paired devices
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null || !btAdapter.isEnabled()) {
            Log.i("check", "ble");
            Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d("TAG", "Enabled");
            bleSc = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            Log.i("scan", "off");
            scanLeDevice(false);
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    /* This routine is called when an activity completes.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode==RESULT_OK){
                Toast.makeText(MainActivity.this, "BlueTooth Turned On", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if (btAdapter != null) {
            Log.println(Log.INFO, "Pause", "shit");
            if (bleSc != null){
                bleSc.stopScan(scanCallback);
            }
            unregisterReceiver(mGattUpdateReceiver);
            Intent intent = new Intent(this, BLEConnection.class);
            stopService(intent);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btAdapter != null) {
            Log.println(Log.INFO, "btList", "kill");
            scanLeDevice(false);

        }
        unbindService(mServiceConnection);
    }


    private void getPermWrapper(){
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Bluetooth");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS/WiFi");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");

        Log.println(Log.INFO, "la", permissionsList.size()+"");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, permission)!= PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission))
                return false;
        }
        return true;
    }

    // Device scan callback.
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice bleDevice = result.getDevice();
            int rssi = result.getRssi();
            String name = bleDevice.getName();
            uuid = result.getScanRecord().getServiceUuids();
            List info = new ArrayList();
            info.add(0, rssi);
            info.add(1, name);
            if (!btDeviceList.containsKey(bleDevice.toString())){
                btDeviceList.put(bleDevice.toString(), info);
                Log.i("Results", btDeviceList.toString());
            }

            ToJSON t = new ToJSON();
            t.mCreateAndSaveFile(btDeviceList.toString());
            Set<String> key = btDeviceList.keySet();
            ArrayList<String> keyL = new ArrayList<>(key);
            for (int i=0; i<keyL.size(); i++){
                t1.append("\nAddress - "+keyL.get(i)+", \nRSSI: "+btDeviceList.get(keyL.get(i)).get(0)+", \nLocal Name: "+btDeviceList.get(keyL.get(i)).get(1));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
}
;}