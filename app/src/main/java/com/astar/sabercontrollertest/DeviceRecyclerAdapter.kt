package com.astar.sabercontrollertest

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.astar.sabercontrollertest.databinding.RowDeviceBinding
import kotlinx.android.synthetic.main.row_device.view.*

class DeviceRecyclerAdapter(
    private val mOnItemClickListener: OnItemClickListener,
    private val mData: MutableList<Pair<BluetoothDevice, Boolean>>
) :RecyclerView.Adapter<BaseViewHolder>() {

    private val mNewData = mutableListOf<Pair<BluetoothDevice, Boolean>>()

    fun appendDevice(device: BluetoothDevice) {
        mData.add(Pair(device, false))
        notifyItemInserted(itemCount - 1)
    }

    fun clearDevices() {
        mData.clear()
        mNewData.clear()
        notifyDataSetChanged()
    }

    fun setItems(newItems: List<Pair<BluetoothDevice, Boolean>>) {
        val result = DiffUtil.calculateDiff(DiffUtilCallback(newItems, mData))
        result.dispatchUpdatesTo(this)
        mData.clear()
        mData.addAll(newItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = RowDeviceBinding.inflate(LayoutInflater.from(parent.context))
        return DeviceViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
            } else {
                // TODO: 14.12.2020 Реализовать: Информация о батарее
                /*val combinedChange =
                    createCombinedPayload(payloads as List<Change<Pair<BluetoothDevice, Boolean>>>)
                val oldData = combinedChange.oldData
                val newData = combinedChange.newData

                if (newData.first.name != oldData.first.name) {
                    holder.itemView.tv_device_name.text = newData.first.name ?: holder.itemView.context.getString(R.string.device_name_unnamed)
                }*/
            }
        }
    }

    override fun getItemCount(): Int = mData.size


    inner class DeviceViewHolder(view: View) : BaseViewHolder(view) {
        override fun bind(dataItem: Pair<BluetoothDevice, Boolean>) {
            itemView.setOnClickListener { mOnItemClickListener.onItemClick(dataItem.first) }
            itemView.tv_device_name.text = dataItem.first.name ?: itemView.context.getString(R.string.device_name_unnamed)
            itemView.tv_device_mac_address.text = dataItem.first.address
        }
    }

    inner class DiffUtilCallback(
        private var oldItems: List<Pair<BluetoothDevice, Boolean>>,
        private var newItems: List<Pair<BluetoothDevice, Boolean>>
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].first.address == newItems[newItemPosition].first.address
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].first.name == newItems[newItemPosition].first.name
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return Change(
                oldItem,
                newItem
            )
        }
    }

    interface OnItemClickListener {
        fun onItemClick(bluetoothDevice: BluetoothDevice)
    }

    interface OnDeviceRecyclerAdapterCallback {
        fun onConnect(device: BluetoothDevice)
    }
}

