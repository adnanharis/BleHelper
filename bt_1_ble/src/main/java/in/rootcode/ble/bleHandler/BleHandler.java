package in.rootcode.ble.bleHandler;

import android.content.Context;

import in.rootcode.ble.BleManager;

/**
 * Created by Adnan Haris on 14/3/18.
 */

public interface BleHandler {

    void bleScan(Context context);

    void stopBleScan();

    final class Common {
        public void gotBleData(String data) {
            BleManager.INSTANCE.gotBleScannedData(data);
        }
    }
}
