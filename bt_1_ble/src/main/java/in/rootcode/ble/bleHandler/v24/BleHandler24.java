package in.rootcode.ble.bleHandler.v24;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;

import in.rootcode.ble.bleHandler.v21.BleHandler21;
import in.rootcode.bt_0_location.in.rootcode.locationhelper.LocationHandler;

/**
 * Created by Adnan Haris on 4/4/18.
 * <p>
 * BLE scan will be same as it is for API-21.
 * It will just make sure about GPS to be ON at the time of scanning for api >= 24.
 */

@TargetApi(Build.VERSION_CODES.N)
public class BleHandler24 extends BleHandler21 {

    public BleHandler24(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter);
    }

    @Override
    public void bleScan(Context context) {
        super.bleScan(context);

        // Enable GPS
        if (context instanceof FragmentActivity) {
            setUpGPS((FragmentActivity) context);
        }
    }

    @Override
    public void stopBleScan() {
        super.stopBleScan();
        cleanUpGPS();
    }

    private void setUpGPS(FragmentActivity activity) {
        LocationHandler.setUp(activity);
    }

    private void cleanUpGPS() {
        LocationHandler.cleanUp();
    }

}