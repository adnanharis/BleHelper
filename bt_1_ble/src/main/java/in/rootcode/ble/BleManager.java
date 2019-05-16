package in.rootcode.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.rootcode.ble.bleHandler.BleHandler;

/**
 * Created by Adnan Haris on 14/3/18.
 */

public enum BleManager {
    INSTANCE;

    private static final String LOG_TAG = BleManager.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BleHandler mBleHandler;
    private boolean mBTFirstState;
    private List<BleCallback> bleListeners = new ArrayList<>();

    public void scan(Context context, BleCallback bleCallback) throws BluetoothNotSupportedException {
        bleListeners.add(bleCallback);
        isBleSupported(context);
        BluetoothAdapter mBTadapter = getBTAdapter();
        mBTFirstState = mBTadapter.isEnabled();
        Log.i(LOG_TAG, "BLE: BT STATE: " + mBTFirstState);
        if (mBTFirstState) {
            Log.i(LOG_TAG, "Bluetooth LE is turned on and ready for communication.");
        } else {
            Log.i(LOG_TAG, "Bluetooth on this device is currently powered off.");
            requestToEnableBT(context, mBTadapter);
            return;
        }

        scanWithApi(context);
    }


    public void stopScan(Context context, BleCallback bleCallback) throws BluetoothNotSupportedException {
        bleListeners.remove(bleCallback);
        // Stop scanning and set btAdapter to original state and unregister receiver
        stopBleScanning();
        // unregisterReceiver(context);
        if (!mBTFirstState) {
            disableBT(getBTAdapter());
        }
    }

    private void requestToEnableBT(Context context, BluetoothAdapter mBTadapter) {
        Log.i(LOG_TAG, "BLE: Enabling BT");
        boolean isEnabling = enableBT(mBTadapter);
        Log.i(LOG_TAG, "BLE: Enabling BT requested");
        if (isEnabling) {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mBTReceiver, filter);
        }
    }

    private void isBleSupported(Context context) throws BluetoothNotSupportedException {
        // Has Ble feature
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i(LOG_TAG, "This device does not support Bluetooth Low Energy.");
            throw new BluetoothNotSupportedException();
        }

        // Api > 18
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.i(LOG_TAG, "This device does not support Bluetooth Low Energy.");
            throw new BluetoothNotSupportedException();
        }
    }

    private BluetoothAdapter getBTAdapter() throws BluetoothNotSupportedException {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter;
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new BluetoothNotSupportedException();
        }
        return mBluetoothAdapter;
    }

    private boolean enableBT(BluetoothAdapter mBluetoothAdapter) {
        if (!mBluetoothAdapter.isEnabled()) {
            return mBluetoothAdapter.enable();
        }
        return true;
    }

    private boolean disableBT(BluetoothAdapter mBluetoothAdapter) {
        if (mBluetoothAdapter.isEnabled()) {
            return mBluetoothAdapter.disable();
        }
        return true;
    }

    private void stopBleScanning() {
        if (mBleHandler != null) {
            mBleHandler.stopBleScan();
            mBleHandler = null;
        }
    }

    private void unregisterReceiver(Context context) {
        try {
            if (!mBTFirstState) {
                context.unregisterReceiver(mBTReceiver);
            }
        } catch (IllegalArgumentException e) {
            // This requires to catch as there is no method to know if receiver is registered
            Log.e(LOG_TAG, "Error", e);
        }
    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(LOG_TAG, "BLE: BT STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(LOG_TAG, "BLE: BT STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(LOG_TAG, "BLE: BT STATE_ON. Ready for communication");
                        scanWithApi(context);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(LOG_TAG, "BLE: BT STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.ERROR:
                        Log.i(LOG_TAG, "BLE: BT ERROR. State is unknown");
                        break;
                }
            }
        }
    };


    private void scanWithApi(Context context) {
        try {
            unregisterReceiver(context);
            BluetoothAdapter bluetoothAdapter = getBTAdapter();
            BleApi bleApi = BleApi.getDefault();
            mBleHandler = getBleHandler(bleApi, bluetoothAdapter);
            mBleHandler.bleScan(context);
        } catch (BluetoothNotSupportedException e) {
            // Here we can handle exception as this a is already thrown by caller
            Log.e(LOG_TAG, "Error while ble scan.", e);
        }
    }

    private BleHandler getBleHandler(BleApi bleApi, BluetoothAdapter bluetoothAdapter) {
        return bleApi.getHandler(bluetoothAdapter);
    }

    public void gotBleScannedData(String data) {
        Iterator<BleCallback> iterator = bleListeners.iterator();
        while (iterator.hasNext()) {
            BleCallback bleCallback = iterator.next();
            bleCallback.onBleDataReceived(data);
        }
    }

}
