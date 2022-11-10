package com.dharmapal.parking_manager_kt

import android.Manifest
import android.R.attr.delay
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dharmapal.parking_manager_kt.Utills.ManagePermissions
import com.dharmapal.parking_manager_kt.adapters.DeviceAdapter
import com.dharmapal.parking_manager_kt.databinding.ActivityDeviceListBinding
import java.lang.reflect.Method


class DeviceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceListBinding
    private var deviceAdapter: DeviceAdapter? = null
    var list = ArrayList<BluetoothDevice>()
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var managePermissions: ManagePermissions
    private lateinit var receiver: BluetoothReceiver
    private var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var delay = 1000

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onResume() {
        Log.d("lcd","resume")
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            val bondedList=bluetoothAdapter.bondedDevices.filter {
                isConnected(it)
            }
            if (bondedList.isNotEmpty()){
                finish()
            }

        }.also { runnable = it }, delay.toLong())

        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

    override fun onStart() {
        super.onStart()
        Log.d("lcd","start")

        val  manager:LocationManager = getSystemService( Context.LOCATION_SERVICE ) as LocationManager
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps()
        }


        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        receiver = BluetoothReceiver()

        val list =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            } else {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this,list,123)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            managePermissions.checkPermissions()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lcd","create")
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


        enableBT()
        getPairedDevice()

        binding.buttonScan.setOnClickListener {
            list.clear()
            discoverDevice()
        }

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123->{
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(applicationContext,"Please Allow All Required Permissions.",Toast.LENGTH_LONG).show()
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                            "package:$packageName"
                        )))
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBT(){

            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(intent)
            }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    @SuppressLint("MissingPermission")
    private fun discoverDevice() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoverDeviceReceiver, filter)
        bluetoothAdapter.startDiscovery()

    }

    private val discoverDeviceReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent!!.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.d("DiscoverDevice1", "STATE CHANGED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("DiscoverDevice2", "Discovery Started")
                    val animation = binding.animationBluetooth
                    binding.animationBluetooth.isVisible = true
                    handler.postDelayed(Runnable {
                        handler.postDelayed(runnable!!, delay.toLong())
                        animation.playAnimation()
                    }.also { runnable = it }, delay.toLong())
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("DiscoverDevice3", "Discovery Finish")
//                    discoverDevice()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        Log.d("DiscoverDevice4", "${device.name} ${device.address}")
                        if (device.name != null && !list.contains(device)) {

                            binding.animationBluetooth.cancelAnimation()
                            handler.removeCallbacks(runnable!!)
                            binding.animationBluetooth.isVisible = false

                            list.add(device)

                            when (device.bondState) {
                                BluetoothDevice.BOND_BONDED -> {
                                    Log.d("DiscoverDevice5", "bonded")
                                }
                                BluetoothDevice.BOND_BONDING -> {
                                    Log.d("DiscoverDevice5", "bonding")
                                }
                            }
                        }
                    }
                }
            }

            deviceAdapter = DeviceAdapter(list) {
                Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_LONG).show()
                it.createBond()

            }
            deviceAdapter.also { binding.newDevices.adapter = it }
        }

    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevice() {

        val arr = bluetoothAdapter.bondedDevices
        Log.d("bondedDevice", arr.size.toString())
        Log.d("bondedDevice", arr.toString())
        for (device in arr) {
            Log.d("bondedDevice", device.name + "  " + device.address + "  " + device.bondState)
            device.removeBond()
        }
        if(arr.isNotEmpty()){
            Toast.makeText(this,"Your Device Already Saved.",Toast.LENGTH_LONG).show()
        }

    }

    private fun BluetoothDevice.removeBond() {
        try {
            javaClass.getMethod("removeBond").invoke(this)
        } catch (e: Exception) {
            Log.e("TAG", "Removing bond has been failed. ${e.message}")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

}

class Discoverability : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action != null) {
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                when (intent.getIntExtra(
                    BluetoothAdapter.EXTRA_SCAN_MODE,
                    BluetoothAdapter.ERROR
                )) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                        Log.d("message", "Connectable")
                    }
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                        Log.d("message", "Discoverable")
                    }
                    BluetoothAdapter.SCAN_MODE_NONE -> {
                        Log.d("message", "NONE")
                    }
                }
            }
        }
    }
}

class BluetoothReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.d("message1", "State On")
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.d("message1", "State Off")
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Log.d("message1", "Turning Off")
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    Log.d("message1", "Turning On")
                }
                BluetoothAdapter.STATE_CONNECTED->{
                    Log.d("message1", "done!!")
                }
            }
        }
    }
}