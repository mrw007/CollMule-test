package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by aigerimzhalgasbekova on 20/04/2017.
 */

public class BLEConnection extends Service {
    private final static String TAG = BLEConnection.class.getSimpleName();

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public static final String COMMAND_KEY = "command_key";
    public static final String COMMAND_START_CONNECTION =
            "command_start_discovery";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BLEConnection getService() {
            return BLEConnection.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (btManager == null) {
            btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (btManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        btAdapter = btManager.getAdapter();
        if (btAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    long timeStart;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (btAdapter!=null) {
            if (intent.getStringExtra(COMMAND_KEY).equals(COMMAND_START_CONNECTION)){
                ArrayList<String> address = intent.getStringArrayListExtra(EXTRA_DATA);
                Log.i("onStart", "onStartcommand");

                timeStart = System.currentTimeMillis();
                Log.i("Connection Time Started", "" + timeStart);

                connectToDevice(address);

            }
        }

        return Service.START_REDELIVER_INTENT;
    }


    Handler mHandler = new Handler();
    BluetoothDevice bdevice;
    private Thread connectionThread;
    private Thread individConThread;

    public void connectToDevice(final ArrayList<String> address) {
                try {
                    for (int i = address.size() - 1; i > -1; i--) {
                        //try {
                        String adr = address.get(i);
                        individConThread = new Thread(new IndividConnection(adr));
                        individConThread.start();
                        Thread.sleep(500);
                    }
                } catch(Exception e){
                    Log.i(TAG, "Gatt server doesn't respond");
                } finally {
                }

    }


    private class IndividConnection implements Runnable{
        BluetoothGatt mGatt;
        String adr;
        public IndividConnection(String address){
            adr = address;
        }
        @Override
        public void run() {
            synchronized (BLEConnection.this){
                try {
                    if (mGatt == null) {
                        bdevice = btAdapter.getRemoteDevice(adr);
                        Log.i("connectToDevice", "connecting to device: " + bdevice.toString());
                        mGatt = bdevice.connectGatt(BLEConnection.this, false, new BluetoothGattCallback() {


                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                Log.i("onConnectionStateChange", "Status: " + status);
                                gatt.discoverServices();
                                super.onConnectionStateChange(gatt, status, newState);
                                String intentAction;
                                if (status == 133){
                                    Log.e(TAG, "Got the status 133 bug, closing gatt");
                                    gatt.close();
                                    return;
                                }
                                if (newState == BluetoothProfile.STATE_CONNECTED) {
                                    intentAction = ACTION_GATT_CONNECTED;
                                    mConnectionState = STATE_CONNECTED;
                                    broadcastUpdate(intentAction);
                                    Log.i(TAG, "Connected to GATT server.");
                                    // Attempts to discover services after successful connection.
                                    Log.i(TAG, "Attempting to start service discovery:" +
                                            mGatt.discoverServices());

                                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                    intentAction = ACTION_GATT_DISCONNECTED;
                                    mConnectionState = STATE_DISCONNECTED;
                                    Log.i(TAG, "Disconnected from GATT server.");
                                    broadcastUpdate(intentAction);
                                }

                            }

                            @Override
                            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                                BluetoothGattService service = gatt.getService(UUID_Service);
                                BluetoothGattCharacteristic temperatureChar = service.getCharacteristic(UUID_TEMPERATURE);
                                gatt.readCharacteristic(temperatureChar);
                                super.onServicesDiscovered(gatt, status);
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                                } else {
                                    Log.w(TAG, "onServicesDiscovered received: " + status);
                                }
                            }


                            @Override
                            public void onCharacteristicRead(BluetoothGatt gatt,
                                                             final BluetoothGattCharacteristic
                                                                     characteristic, int status) {
                                final String value = characteristic.getStringValue(0);
                                if (status==BluetoothGatt.GATT_SUCCESS){
                                    if (UUID_HUMIDITY.equals(characteristic.getUuid())){
                                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, adr);
                                    } else if (UUID_PM.equals(characteristic.getUuid())){
                                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, adr);
                                    } else {
                                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, adr);
                                    }

                                }
                                //Log.i("Characteristics", ""+characteristics.size());
                                BluetoothGattService service = gatt.getService(UUID_Service);
                                readNextCharacteristic(gatt, characteristic);
                                super.onCharacteristicRead(gatt, characteristic, status);
                            }

                            boolean tempRead = false;
                            boolean humRead = false;
                            boolean PMRead = false;

                            private void readNextCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                                BluetoothGattService service = gatt.getService(UUID_Service);
                                Log.i("preread state", tempRead+" "+humRead+" "+PMRead);
                                if (UUID_HUMIDITY.equals(characteristic.getUuid())) {
                                    BluetoothGattCharacteristic temperatureChar = service.getCharacteristic(UUID_TEMPERATURE);
                                    gatt.readCharacteristic(temperatureChar);
                                    tempRead = true;
                                } else if (UUID_PM.equals(characteristic.getUuid())){
                                    BluetoothGattCharacteristic humidityChar = service.getCharacteristic(UUID_HUMIDITY);
                                    gatt.readCharacteristic(humidityChar);
                                    humRead = true;
                                } else {
                                    BluetoothGattCharacteristic pmChar = service.getCharacteristic(UUID_PM);
                                    gatt.readCharacteristic(pmChar);
                                    PMRead = true;
                                }

                                if (tempRead==true && humRead==true && PMRead==true){
                                    mGatt.close();
                                    mGatt.disconnect();
                                    mGatt = null;
                                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                                    long timeElapsed = System.currentTimeMillis() - timeStart;
                                    Log.i("Connection Time Elapsed", "" + timeElapsed);
                                    Log.i("read state", tempRead+" "+humRead+" "+PMRead);
                                    tempRead =false;
                                    humRead = false;
                                    PMRead = false;
                                    Log.i("ReadStatus", ""+tempRead+humRead+PMRead);
                                }
                                //Log.i("mGatt", mGatt.toString());
                            }

                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                                BluetoothGattCharacteristic
                                                                        characteristic) {
                                float char_float_value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT,1);
                                Log.i("onCharacteristicChanged", Float.toString(char_float_value));
                                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, adr);
                            }

                        });
                        Log.i("GATT", "new gatt");

                    } else {
                        Log.i("why", "!null");
                        Log.i("next address", adr);
                    }

                } catch (Exception e){
                    Log.i(TAG, "individual connection failed");
                } finally {
                }
            }
        }

    }
    private static final UUID UUID_Service = UUID.fromString("19fc95c0-c111-11e3-9904-0002a5d5c51b");
    private static final UUID UUID_TEMPERATURE = UUID.fromString("21fac9e0-c111-11e3-9904-0002a5d5c51b");
    private static final UUID UUID_HUMIDITY = UUID.fromString("31fac9e0-c111-11e3-9904-0002a5d5c51b");
    private static final UUID UUID_PM = UUID.fromString("41fac9e0-c111-11e3-9904-0002a5d5c51b");


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, final String address) {
        final Intent intent = new Intent(action);

        if (UUID_TEMPERATURE.equals(characteristic.getUuid())) {
            String temp = characteristic.getStringValue(0);
            Log.i(TAG, "Received temperature: "+ temp+" - "+address);
            intent.putExtra("Address", address);
            intent.putExtra("Temperature:", temp+" - "+address);
        } else if (UUID_HUMIDITY.equals(characteristic.getUuid())){
            String hum = characteristic.getStringValue(0);
            intent.putExtra("Humidity:", hum+" - "+address);
        } else if (UUID_PM.equals(characteristic.getUuid())){
            String pm = characteristic.getStringValue(0);
            Log.i(TAG, "Received PM: "+pm+" - "+address);
            intent.putExtra("PM:", pm+" - "+address);
        }
        sendBroadcast(intent);
    }

}
