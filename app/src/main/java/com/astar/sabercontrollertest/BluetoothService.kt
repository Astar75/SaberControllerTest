package com.astar.sabercontrollertest

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.astar.sabercontrollertest.blue.BluetoothDeviceModel
import com.astar.sabercontrollertest.blue.ConnectionManager
import no.nordicsemi.android.support.v18.scanner.*

class BluetoothService : Service() {

    private val mScanner: BluetoothLeScannerCompat by lazy {
        BluetoothLeScannerCompat.getScanner()
    }

    private val mConnectionManager: ConnectionManager by lazy {
        ConnectionManager(applicationContext)
    }

    private var _isScanning: Boolean = false
    val isScanning: Boolean
        get() = _isScanning

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            sendBluetoothDevice(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "onScanFailed: Error code = $errorCode")
            sendBluetoothStatus(Constants.BLE.ACTION_FOUND_ERROR, null)
        }
    }

    private val mConnectionManagerCallback = object : ConnectionManager.ConnectionManagerCallback {
        override fun onConnect(device: BluetoothDevice) {
            sendBluetoothStatus(Constants.BLE.ACTION_CONNECTED, device)
        }

        override fun onDisconnect(device: BluetoothDevice) {
            sendBluetoothStatus(Constants.BLE.ACTION_DISCONNECTED, device)
        }

        override fun onConnectFailed(device: BluetoothDevice, reason: Int) {
            sendBluetoothStatus(Constants.BLE.ACTION_CONNECTED_FAILED, device)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mConnectionManager.addCallback(mConnectionManagerCallback)
    }

    private fun sendBluetoothDevices(scanResults: MutableList<ScanResult>) {
        val devices = mutableListOf<Pair<BluetoothDevice, Int>>()
        scanResults.map { result ->
            devices.add(Pair(result.device, result.rssi))
        }
        val intent = Intent()
        intent.action = Constants.BLE.ACTION_FOUND_DEVICE
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDeviceModel(devices))
    }

    private fun sendBluetoothDevice(result: ScanResult) {
        val intent = Intent()
        intent.action = Constants.BLE.ACTION_FOUND_DEVICE
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, result.device)
        sendBroadcast(intent)
    }

    private fun sendBluetoothStatus(actionStatus: String, device: BluetoothDevice?) {
        val intent = Intent()
        intent.action = actionStatus
        device?.let { intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device) }
        sendBroadcast(intent)
    }


    fun searchDevice() {
        _isScanning = true
        Log.d(BluetoothService::class.simpleName, "Запускаем поиск")
        val settings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            //.setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(false)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()
        // val filters = listOf<ScanFilter>()
        mScanner.startScan(null, settings, mScanCallback)
        sendBluetoothStatus(Constants.BLE.ACTION_START_SCAN, null)
    }

    fun stopSearch() {
        _isScanning = false
        mScanner.stopScan(mScanCallback)
        sendBluetoothStatus(Constants.BLE.ACTION_STOP_SCAN, null)
    }

    fun sendColor(device: BluetoothDevice?, color: ByteArray) {
        device?.let { mConnectionManager.sendColor(it, color) }
    }

    fun connectToDevice(device: BluetoothDevice?) {
        device?.let {
            Log.d(TAG, "Соединение с устройством ")
            mConnectionManager.connect(it)
        }
    }

    fun disconnectDevice(device: BluetoothDevice?) {
        device?.let { mConnectionManager.disconnect(it) }
    }

    companion object {
        const val TAG = "BluetoothService"
    }
}