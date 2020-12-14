package com.astar.sabercontrollertest.blue

import android.bluetooth.BluetoothDevice
import java.io.Serializable


data class BluetoothDeviceModel(
    val mBluetoothDevices: MutableList<Pair<BluetoothDevice, Int>>
): Serializable
