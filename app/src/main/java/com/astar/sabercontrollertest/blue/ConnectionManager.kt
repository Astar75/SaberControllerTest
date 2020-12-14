package com.astar.sabercontrollertest.blue

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.observer.ConnectionObserver

class ConnectionManager(private val mContext: Context) : ConnectionObserver {

    private val mBleManagers = HashMap<BluetoothDevice, SaberBleManager>()
    private val mManagedDevices = mutableListOf<BluetoothDevice>()
    private var mCallback: ConnectionManagerCallback? = null

    fun addCallback(callback: ConnectionManagerCallback) {
        mCallback = callback
    }

    fun connect(device: BluetoothDevice) {
        if (mManagedDevices.contains(device)) {
            return
        }
        mManagedDevices.add(device)

        var manager: SaberBleManager? = mBleManagers[device]
        if (manager == null) {
            manager = SaberBleManager(mContext)
            manager.setConnectionObserver(this@ConnectionManager)
            mBleManagers[device] = manager
        }

        manager.connect(device)
            .retry(3, 100)
            .useAutoConnect(true)
            .timeout(100000)
            .fail { d, _ ->
                mManagedDevices.remove(d)
                mBleManagers.remove(d)
            }
            .enqueue()
    }

    fun disconnect(device: BluetoothDevice) {
        val manager = mBleManagers[device]
        if (manager != null && manager.isConnected) {
            manager.disconnect().enqueue()
        }
        mManagedDevices.remove(device)
        mBleManagers.remove(device)
    }

    fun isConnected(device: BluetoothDevice): Boolean {
        val manager = mBleManagers[device]
        return manager?.isConnected ?: false
    }

    fun getConnectionState(device: BluetoothDevice): Int {
        val manager = mBleManagers[device]
        return manager?.connectionState ?: BluetoothGatt.STATE_DISCONNECTED
    }

    fun sendColor(device: BluetoothDevice, color: ByteArray) {
        val manager = mBleManagers[device]
        if (manager != null && manager.isConnected) {
            manager.sendColor(color)
        }
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d(TAG, "Соединение к ${device.address} ")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d(TAG, "Устройство соединено ${device.address}")
        mCallback?.onConnect(device)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "Произошла ошибка при подключении к ${device.address}, reason = $reason")
        mCallback?.onConnectFailed(device, reason)
    }

    override fun onDeviceReady(device: BluetoothDevice) {

    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {

    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "Разъединено от ${device.address}")
        mCallback?.onDisconnect(device)
    }

    interface ConnectionManagerCallback {
        fun onConnect(device: BluetoothDevice)
        fun onDisconnect(device: BluetoothDevice)
        fun onConnectFailed(device: BluetoothDevice, reason: Int)
    }

    companion object {
        const val TAG = "ConnectionManager"
    }
}