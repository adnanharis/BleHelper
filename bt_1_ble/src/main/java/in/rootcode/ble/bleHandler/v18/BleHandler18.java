package in.rootcode.ble.bleHandler.v18;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import in.rootcode.ble.bleHandler.BleHandler;

/**
 * Created by Adnan Haris on 14/3/18.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleHandler18 implements BleHandler {

    protected BluetoothAdapter mBluetoothAdapter;
    protected Common common;

    private boolean mScanning;

    private static final String LOG_TAG = BleHandler18.class.getSimpleName();

    public BleHandler18(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        common = new Common();
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
        Log.i(LOG_TAG, "BLE: Scanning stopped v18");
        stopScan();
    }

    private void startLeScan() {
        Log.i(LOG_TAG, "BLE: Scanning started v18");
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mScanning = true;
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            // scanComplete();
        }
        mScanning = false;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    Log.i(LOG_TAG, "Le Device: " + device.getName() + " : " + device.getAddress());
//                    Log.i(LOG_TAG, "Scan record: " + scanRecord);

                    if (!device.getAddress().equals("B8:27:EB:64:30:D0")) {
                        return;
                    }

                    byte[] manuData = getManufacturerData(scanRecord);
                    if (manuData == null) {
                        return;
                    }

                    // TODO: Need to fix this. Very small market shares
                    /*int companyId = (manuData[0] & 0xF) * (manuData[1] & 0xF);
                    if (companyId != 65535) {
                        common.gotBleData(manuData);
                    }*/

                }
            };

    private byte[] getManufacturerData(final byte[] scanRecord) {
        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);
        List<UUID> uuids = new ArrayList<>();
        byte[] mfgData = null;

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            type = type & 0xFF; // up scaling -ve byte values to correct it for int like -1 is for 0xFF
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++] & 0xFF;
                        uuid16 |= (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                                    offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e(LOG_TAG, "BlueToothDeviceFilter.parseUUID", e);
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                case 0xFF:  // Manufacturer Specific Data
                    Log.d(LOG_TAG, "Manufacturer Specific Data size:" + len + " bytes");
                    int i = 0;
                    mfgData = new byte[len - 1]; // -1 is to exclude type
                    while (len > 1) {
                        if (i < 32) {
                            mfgData[i++] = advertisedData[offset++]; // & 0xFF;
                        }
                        len -= 1;
                    }
                    Log.d(LOG_TAG, "Manufacturer Specific Data saved." + Arrays.toString(mfgData));
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return mfgData;
    }

}
