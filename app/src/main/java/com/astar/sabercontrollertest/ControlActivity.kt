package com.astar.sabercontrollertest

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.astar.sabercontrollertest.databinding.ActivityControlBinding
import com.skydoves.colorpickerview.listeners.ColorListener

class ControlActivity : BaseActivity() {

    private lateinit var mBinding: ActivityControlBinding
    private var mConnectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()

        mConnectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        Log.d(TAG, "Device: ${mConnectedDevice?.address}")
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(mBroadcastReceiver, IntentFilter().apply {
            addAction(Constants.BLE.ACTION_CONNECTED)
            addAction(Constants.BLE.ACTION_DISCONNECTED)
            addAction(Constants.BLE.ACTION_CONNECTED_FAILED)
        })
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectDevice(mConnectedDevice)
    }

    override fun onServiceBound() {
        connectToDevice(mConnectedDevice)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(MainActivity.TAG, "onReceive: ${intent?.action}")
            when (intent?.action) {
                Constants.BLE.ACTION_CONNECTED -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Toast.makeText(
                        applicationContext,
                        "Подключены к устройству ${device?.address}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Constants.BLE.ACTION_DISCONNECTED -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Toast.makeText(
                        applicationContext,
                        "Разъединено с ${device?.address}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val mColorListener =
        ColorListener { color, _ ->
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            mBinding.colorPreviewText.text = String.format(
                "RGB: R:%d G:%d B:%d\nHEX: #%02X%02X%02X",
                red, green, blue, red, green, blue
            )
            sendColor(byteArrayOf(red.toByte(), green.toByte(), blue.toByte()))
        }

    private fun initViews() {
        with(mBinding) {
            colorPicker.colorListener = mColorListener
        }
    }

    private fun connectToDevice(device: BluetoothDevice?) {
        if (mBound) {
            Log.d(TAG, "Соединение с устройством")
            mService.connectToDevice(device)
        } else {
            Log.d(TAG, "Сервис не присоединен")
        }
    }

    private fun disconnectDevice(device: BluetoothDevice?) {
        if (mBound) {
            mService.disconnectDevice(device)
        }
    }

    private fun sendColor(color: ByteArray) {
        if (mBound) {
            mService.sendColor(mConnectedDevice, color)
            Log.d(TAG, "Отправка цвета")
        } else {
            Log.e(TAG, "Сервис не присоединен")
        }
    }

    companion object {
        const val TAG = "ControlActivity"

        @JvmStatic
        fun start(context: Context, device: BluetoothDevice) {
            val starter = Intent(context, ControlActivity::class.java)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, device)
            context.startActivity(starter)
        }
    }
}