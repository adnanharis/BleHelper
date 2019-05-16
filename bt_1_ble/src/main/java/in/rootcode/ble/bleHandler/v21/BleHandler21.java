package in.rootcode.ble.bleHandler.v21;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;

import in.rootcode.ble.bleHandler.v18.BleHandler18;

/**
 * Created by Adnan Haris on 14/3/18.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleHandler21 extends BleHandler18 {

    private static final String LOG_TAG = BleHandler21.class.getSimpleName();
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;

    public BleHandler21(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    @Override
    public void bleScan(Context context) {
        if (mScanning) {
            return;
        }

        startLeScan();
    }

    @Override
    public void stopBleScan() {
        stopScan();
    }

    private void startLeScan() {
        Log.i(LOG_TAG, "BLE: Scanning started v21");

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            // This can be null when even BT is oFF but api returned it as ON.
            // This is seen while debugging. This is not observed in non-debug mode
            return;
        }

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // fast scanning
                .build();
        mBluetoothLeScanner.startScan(null, settings, mLeScanCallback);
        mScanning = true;
    }

    private void stopScan() {
        Log.i(LOG_TAG, "BLE: Scanning stopped v21");
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
            // scanComplete();
        }
        mScanning = false;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(LOG_TAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            String message = device.getAddress() + " | " + device.getName() + " | " + scanRecord;
            common.gotBleData(message);

            /*
            Log.i(LOG_TAG, "Scan record: " + scanRecord);
            if (scanRecord == null) {
                return;
            }

            byte[] manuData = scanRecord.getManufacturerSpecificData(65535); // For 0xFF
            if (manuData == null) {
                return;
            }

            common.gotBleData(manuData);
            */
        }
    };


}
