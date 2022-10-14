package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dharmapal.parking_manager_kt.databinding.ActivityDeviceListBinding

class DeviceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceListBinding
    private var extraDeviceAddress = "device_address"

    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager: BluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(BluetoothManager::class.java)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        binding= ActivityDeviceListBinding.inflate(layoutInflater)
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(binding.root)


        // Set result CANCELED inCase the user backs out
        setResult(RESULT_CANCELED)

        binding.buttonScan.setOnClickListener {
            doDiscovery(bluetoothAdapter!!)
            it.visibility = View.GONE
        }


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        // Find and set up the ListView for paired devices
        val pairedListView = findViewById<ListView>(R.id.paired_devices)
        pairedListView.adapter = mPairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener


        // Find and set up the ListView for newly discovered devices
        val newDevicesListView = findViewById<ListView>(R.id.new_devices)
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener


        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)


        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get a set of currently paired devices

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.isNotEmpty()) {
            binding.titlePairedDevices.visibility = View.VISIBLE
            for (device in pairedDevices) {
                mPairedDevicesArrayAdapter!!.add(
                    """
                ${device.name}
                ${device.address}
                """.trimIndent()
                )
            }
        } else {
            val noDevices = resources.getText(R.string.none_paired).toString()
            mPairedDevicesArrayAdapter!!.add(noDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (mService != null) {
//            mService.cancelDiscovery()
//        }
//        mService = null
        unregisterReceiver(mReceiver)
    }


    /**
     * Start device discover with the BluetoothAdapter
     */
    @SuppressLint("MissingPermission")
    private fun doDiscovery(bluetoothAdapter: BluetoothAdapter) {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true)
        setTitle(R.string.scanning)

        // Turn on sub-title for new devices
        binding.titleNewDevices.visibility = View.VISIBLE

        val myPairedDevices = bluetoothAdapter.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if (myPairedDevices.isNotEmpty())
        {
            for ( device:BluetoothDevice in myPairedDevices)
                list.add(device)

            //list.add(device.name() + "\n" + device.address())

            Log.i("Device", "This is message $list")

        }
        else {
            Toast.makeText(applicationContext, " NO PAIRED DEVICES FOUND", Toast.LENGTH_LONG).show()

        }

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery()
    }


    // The on-click listener for all devices in the ListViews
    private val mDeviceClickListener =
        OnItemClickListener { _, v, _, _ ->
            //����б�������豸
            // Cancel discovery because it's costly and we're about to connect
//            mService.cancelDiscovery()

            // Get the device MAC address, which is the last 17 chars in the View
            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)

            // Create the result Intent and include the MAC address
            val intent = Intent()
            intent.putExtra(extraDeviceAddress, address)
            Log.d("���ӵ�ַ", address)

            // Set result and finish this Activity
            setResult(RESULT_OK, intent)
            finish()
        }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add(
                        """
                        ${device.name}
                        ${device.address}
                        """.trimIndent()
                    )
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle(R.string.select_device)
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = resources.getText(R.string.none_found).toString()
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }
}