package com.astar.sabercontrollertest.blue;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;

public class SaberBleManager extends BleManager {

    public static final String TAG = "SaberBleManager";

    public static final UUID SERVICE_UUID = UUID.fromString("d4d4dc12-0493-44fa-bc55-477388a6565c");
    public static final UUID COLOR_UUID = UUID.fromString("879928f7-6a26-4c85-bf73-48f141198c83");
    public static final int MTU_DEFAULT = 507;


    private BluetoothGattCharacteristic mColorCharacteristic;

    public SaberBleManager(@NonNull Context context) {
        super(context);
    }

    public void sendColor(byte[] color) {
        Log.d(TAG, "sendColor: Отправка цвета");
        byte[] colorArray = new byte[4];
        colorArray[0] = 0x01;
        System.arraycopy(color, 0, colorArray, 1, color.length);

        writeCharacteristic(mColorCharacteristic, colorArray)
                .split()
                .enqueue();
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new SaberBleManagerCallback();
    }

    private class SaberBleManagerCallback extends BleManagerGattCallback {

        @Override
        protected void initialize() {
            beginAtomicRequestQueue()
                    .add(requestMtu(MTU_DEFAULT)
                            .with(((device, mtu) -> Log.i(TAG, "initialize: MTU set to " + mtu)))
                            .fail(((device, status) -> Log.w(TAG, "initialize: Request MTU not supported: " + status)))
                    )
                    .add(setPreferredPhy(PhyRequest.PHY_LE_1M_MASK, PhyRequest.PHY_LE_1M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                        .fail((device, status) -> Log.i(TAG, "initialize: Set Phy value error"))
                    )
                    .done(device -> Log.d(TAG, "initialize: Target initialized"))
                    .enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(SERVICE_UUID);

            if (service != null) {
                mColorCharacteristic = service.getCharacteristic(COLOR_UUID);
            }

            return service != null;
            // return true;
        }

        @Override
        protected void onDeviceDisconnected() {
            mColorCharacteristic = null;
        }
    }
}
