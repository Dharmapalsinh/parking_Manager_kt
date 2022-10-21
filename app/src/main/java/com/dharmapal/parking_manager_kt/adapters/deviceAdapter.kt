package com.dharmapal.parking_manager_kt.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dharmapal.parking_manager_kt.R
import com.dharmapal.parking_manager_kt.models.PriceModel

class DeviceAdapter(private val list: List<BluetoothDevice>, val onclick:(BluetoothDevice)->Unit) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    var rowIndex = -1

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var devicename: TextView

        init {
            devicename = itemView.findViewById(R.id.dvName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.device_name, parent, false)
        return DeviceViewHolder(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val model=list[position]
        holder.devicename.text=model.name
        holder.devicename.setOnClickListener {
            onclick(model)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}