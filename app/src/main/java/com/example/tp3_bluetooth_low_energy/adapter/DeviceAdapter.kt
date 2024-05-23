package com.example.tp3_bluetooth_low_energy.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tp3_bluetooth_low_energy.databinding.DeviceItemLayoutBinding

class DeviceAdapter(
    private var bleDevicesFoundList: ArrayList<BluetoothDevice>,
    private val onDeviceClickedDA: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = DeviceItemLayoutBinding.inflate(LayoutInflater.from(parent.context))
        return DeviceViewHolder(
            itemView = binding.root,
            onDeviceClickedDVH = { it -> onDeviceClickedDA(bleDevicesFoundList[it]) }
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = bleDevicesFoundList[position]
        holder.bind(device)
    }

    override fun getItemCount(): Int {
        return bleDevicesFoundList.size
    }

    fun addDevice(device: BluetoothDevice) {
        // Ajoute l'appareil Bluetooth à la liste
        bleDevicesFoundList.add(device)

        // Appelle la méthode notifyItemInserted() de l'adaptateur pour signaler que la liste a été mise à jour
        notifyItemInserted(bleDevicesFoundList.size - 1)
    }
}