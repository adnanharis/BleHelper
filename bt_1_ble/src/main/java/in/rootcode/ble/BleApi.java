package in.rootcode.ble;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.support.annotation.NonNull;

import in.rootcode.ble.bleHandler.BleHandler;
import in.rootcode.ble.bleHandler.v18.BleHandler18;
import in.rootcode.ble.bleHandler.v21.BleHandler21;
import in.rootcode.ble.bleHandler.v24.BleHandler24;

/**
 * Created by Adnan Haris on 14/3/18.
 */

public enum BleApi {
    V_21,
    V_18,
    V_24;

    private BleHandler mCachedHandler;

    @NonNull
    public static BleApi getDefault() {
        if (V_24.isSupported()) {
            return V_24;
        } else if (V_21.isSupported()) {
            return V_21;
        } else if (V_18.isSupported()) {
            return V_18;
        } else {
            throw new IllegalStateException("Ble not supported below Api 18");
        }
    }

    public BleHandler getHandler(BluetoothAdapter bluetoothAdapter) {
        if (mCachedHandler == null) {
            mCachedHandler = createHandler(bluetoothAdapter);
        }
        return mCachedHandler;
    }

    private boolean isSupported() {
        switch (this) {
            case V_24:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
            case V_21:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
            case V_18:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

            default:
                throw new IllegalStateException("Ble not supported");
        }
    }

    private BleHandler createHandler(BluetoothAdapter bluetoothAdapter) {
        switch (this) {
            case V_24:
                return new BleHandler24(bluetoothAdapter);
            case V_21:
                return new BleHandler21(bluetoothAdapter);
            case V_18:
                return new BleHandler18(bluetoothAdapter);

            default:
                throw new IllegalStateException("Ble not supported");
        }
    }

}
