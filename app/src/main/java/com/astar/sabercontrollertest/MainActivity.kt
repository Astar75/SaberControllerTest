package com.astar.sabercontrollertest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.astar.sabercontrollertest.blue.BluetoothDeviceModel
import com.astar.sabercontrollertest.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: DeviceRecyclerAdapter

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: ${intent?.action}")
            when (intent?.action) {
                Constants.BLE.ACTION_START_SCAN -> {
                    mAdapter.clearDevices()
                }
                Constants.BLE.ACTION_FOUND_DEVICE -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    addDevice(device)
                }
            }
        }
    }

    private val mBluetoothDeviceAdapterCallback =
        object : DeviceRecyclerAdapter.OnDeviceRecyclerAdapterCallback {
            override fun onConnect(device: BluetoothDevice) {
                connectToDevice(device)
            }
        }

    override fun onStart() {
        super.onStart()
        registerBluetoothServiceBroadcast()
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        unregisterBluetoothServiceBroadcast()
    }

    override fun onServiceBound() {}

    private fun initViews() {
        val data = mutableListOf<Pair<BluetoothDevice, Boolean>>()
        mBluetoothAdapter.bondedDevices.map { bondedDevice ->
            data.add(Pair(bondedDevice, false))
        }
        mAdapter = DeviceRecyclerAdapter(
            object : DeviceRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(bluetoothDevice: BluetoothDevice) {

                }
            },
            data
        )
        // mAdapter.addCallback(mBluetoothDeviceAdapterCallback)

        with(mBinding) {
            with(recyclerDeviceView) {
                setHasFixedSize(true)
                adapter = mAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        this@MainActivity,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }
            fabSearch.setOnClickListener {
                if (isPermissionGranted()) {
                    searchDevices()
                } else {
                    requestPermissions()
                }
            }
        }
    }

    private fun registerBluetoothServiceBroadcast() {
        val filter = IntentFilter().apply {
            addAction(Constants.BLE.ACTION_START_SCAN)
            addAction(Constants.BLE.ACTION_STOP_SCAN)
            addAction(Constants.BLE.ACTION_FOUND_ERROR)
            addAction(Constants.BLE.ACTION_FOUND_DEVICE)
        }
        registerReceiver(mBroadcastReceiver, filter)
    }

    private fun unregisterBluetoothServiceBroadcast() {
        unregisterReceiver(mBroadcastReceiver)
    }

    private fun isPermissionGranted() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_PERMISSION_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_LOCATION
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            searchDevices()
        } else {
            requestPermissions()
        }
    }

    private fun searchDevices() {
        if (mBound) {
            if (mService.isScanning)
                mService.stopSearch()
            else
                mService.searchDevice()
        }
        updateUi()
    }

    private fun updateUi() {
        if (mBound && mService.isScanning) {
            mBinding.fabSearch.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_btn_stop,
                    theme
                )
            )
        } else {
            mBinding.fabSearch.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_btn_search,
                    theme
                )
            )
        }
    }

    private fun addDevice(device: BluetoothDevice?) {
        if (device != null) {
            // mAdapter.appendDevice(device)
            mAdapter.setItems(listOf(Pair(device, false)))
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        mService.stopSearch()
        ControlActivity.start(this, device)
    }

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_PERMISSION_LOCATION = 10
    }
}