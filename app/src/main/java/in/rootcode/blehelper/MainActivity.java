package in.rootcode.blehelper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import in.rootcode.ble.BleCallback;
import in.rootcode.ble.BleManager;
import in.rootcode.ble.BluetoothNotSupportedException;


/**
 * Created by Adnan Haris on 7/3/19.
 */

public class MainActivity extends AppCompatActivity implements BleCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 234;

    // region activity life cycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();
        proceedWithBleScan();
    }

    @Override
    public void onStop() {
        stopBleScan(MainActivity.this);
        super.onStop();
    }
    // endregion

    private void proceedWithBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Ble needs location for le scan
            // TODO: Seprate permission code from MainActivity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(false);
                    alertBuilder.setTitle("Location Required");
                    alertBuilder.setMessage("LE Beacons are often associated with location. " +
                            "In order to scan for BLE packets, you need to grant the location permission.");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
                return;
            }
        }

        startBleScan();
    }

    private void startBleScan() {
        try {
            BleManager.INSTANCE.scan(MainActivity.this, MainActivity.this);
        } catch (BluetoothNotSupportedException e) {
            Log.d(LOG_TAG, "BLE not supported", e);
        }
    }

    private void stopBleScan(Context context) {
        try {
            BleManager.INSTANCE.stopScan(context, MainActivity.this);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "Unable to release not yet registered receiver", e);
        } catch (BluetoothNotSupportedException e) {
            Log.d(LOG_TAG, "BLE not supported", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Location permission granted");
                    proceedWithBleScan();
                } else {
                    Log.i(LOG_TAG, "Location permission rejected / not yet taken");
                    proceedWithBleScan();
                }
            }
        }
    }

    @Override
    public void onBleDataReceived(String data) {
        if (data == null) {
            return;
        }

        Log.i(LOG_TAG, "Data: " + data);
    }
}
