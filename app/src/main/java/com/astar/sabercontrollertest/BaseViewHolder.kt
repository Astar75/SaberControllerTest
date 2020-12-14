package com.astar.sabercontrollertest

import android.bluetooth.BluetoothDevice
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    abstract fun bind(dataItem: Pair<BluetoothDevice, Boolean>)
}