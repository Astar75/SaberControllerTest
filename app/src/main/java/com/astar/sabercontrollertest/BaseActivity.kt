package com.astar.sabercontrollertest

import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity: AppCompatActivity() {

    protected lateinit var mService: BluetoothService
    protected var mBound = false

    private val mBluetoothServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as BluetoothService.LocalBinder
            mService = binder.getService()
            mBound = true
            onServiceBound()
            Log.d(TAG, "onServiceConnected: Сервис присоединен")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound = false
            Log.d(TAG, "onServiceDisconnected: Сервис отсоединен ")
        }
    }

    abstract fun onServiceBound()

    override fun onStart() {
        super.onStart()
        enableBluetooth()
        bindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH_CODE && resultCode == RESULT_CANCELED) {
            Toast.makeText(
                applicationContext,
                "Для работы приложения требуется включить Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun bindService() {
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, mBluetoothServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (mBound) {
            mService.stopSearch()
            unbindService(mBluetoothServiceConnection)
        }
    }

    private fun enableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH_CODE)
    }

    companion object {
        const val TAG = "BaseActivity"
        const val REQUEST_ENABLE_BLUETOOTH_CODE = 111
    }

}