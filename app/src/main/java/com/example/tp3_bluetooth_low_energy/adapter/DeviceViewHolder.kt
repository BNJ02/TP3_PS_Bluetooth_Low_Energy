package com.example.tp3_bluetooth_low_energy.adapter

import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.tp3_bluetooth_low_energy.R

class DeviceViewHolder (
    private val itemView: View,
    private val onDeviceClickedDVH: (Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    val elementDeviceConstraint: ConstraintLayout = itemView.findViewById(R.id.description)
    val elementDeviceName: TextView = itemView.findViewById(R.id.device_name)
    val elementDeviceAddress: TextView = itemView.findViewById(R.id.device_address)

    init {
        elementDeviceConstraint.setOnClickListener {
            onDeviceClickedDVH(adapterPosition)
        }
    }

    fun bind(device: BluetoothDevice) {
        elementDeviceName.text = device.name ?: "Unknown"
        elementDeviceAddress.text = device.address
    }
}