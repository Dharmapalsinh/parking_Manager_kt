package com.dharmapal.parking_manager_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.dharmapal.parking_manager_kt.adapters.DeviceAdapter
import com.dharmapal.parking_manager_kt.databinding.ActivityDeviceListBinding
import java.util.*


class DeviceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceListBinding

    private var deviceAdapter: DeviceAdapter? = null
    var list = ArrayList<BluetoothDevice>()
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var wifiManager: WifiManager
//    private lateinit var wifiAdapter: wifiAdapter
    lateinit var receiver: BluetoothReceiver
    lateinit var receiver2: Discoverability

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        receiver = BluetoothReceiver()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        103
                    )
                }
                else{
                    enableBT()
                    getPairedDevice()
                }
            }
            else{
                enableBT()
                getPairedDevice()
            }
            when (ContextCompat.checkSelfPermission(
                baseContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
            )) {
                PackageManager.PERMISSION_DENIED -> androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("RunTime permission")
                    .setMessage("Give permission")
                    .setNeutralButton("Okay", DialogInterface.OnClickListener { dialog, which ->
                        if (ContextCompat.checkSelfPermission(
                                baseContext,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                                101
                            )
                        }
                    }).show()
                //.findViewById<TextView>(R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()

                PackageManager.PERMISSION_GRANTED -> {
                    Log.d("DiscoverDevice", "Permission Granted")
                }
            }

            when (ContextCompat.checkSelfPermission(
                baseContext, Manifest.permission.ACCESS_FINE_LOCATION
            )) {
                PackageManager.PERMISSION_DENIED -> androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("RunTime permission")
                    .setMessage("Give permission")
                    .setNeutralButton("Okay", DialogInterface.OnClickListener { dialog, which ->
                        if (ContextCompat.checkSelfPermission(
                                baseContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                102
                            )
                        }
                    }).show()
                //.findViewById<TextView>(R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()

                PackageManager.PERMISSION_GRANTED -> {
                    Log.d("DiscoverDevice", "Permission Granted")
                }
            }


        }
        else{

        }

        val  manager:LocationManager = getSystemService( Context.LOCATION_SERVICE ) as LocationManager
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps()
        }


//        enableBT()
//        getPairedDevice()

        binding.buttonScan.setOnClickListener {
            list.clear()
            discoverDevice()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==103){
            enableBT()
            getPairedDevice()
        }
        else if (requestCode==104){
            discoverDevice()
        }
    }
    @SuppressLint("MissingPermission")
    private fun enableBT(){

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(intent)

            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(receiver, intentFilter)
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No"
            ) { dialog, id -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun discoverDevice() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoverDeviceReceiver, filter)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.BLUETOOTH_SCAN),
                    104
                )
            }
            else{
                bluetoothAdapter.startDiscovery()
            }
        }
        else{
            bluetoothAdapter.startDiscovery()
        }
    }


    private val discoverDeviceReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            var action = intent!!.action

            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.d("DiscoverDevice1", "STATE CHANGED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("DiscoverDevice2", "Discovery Started")

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("DiscoverDevice3", "Discovery Finish")
                }
                BluetoothDevice.ACTION_FOUND -> {

                    val device =
                        intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        Log.d("DiscoverDevice4", "${device.name} ${device.address}")
                        if (device.name != null) {
                            list.add(device)
                        }
                    }
                }
            }
            deviceAdapter = DeviceAdapter(list) {
                Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_LONG).show()
                it.createBond()
            }
            binding.newDevices.adapter = deviceAdapter
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
    }

    private fun BluetoothDevice.removeBond() {
        try {
            javaClass.getMethod("removeBond").invoke(this)
        } catch (e: Exception) {
            Log.e("TAG", "Removing bond has been failed. ${e.message}")
        }
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
            }
        }
    }
}
//    private var extraDeviceAddress = "device_address"
//
//    private var deviceAdapter:DeviceAdapter? =null
//    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
//    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null
//
//    @SuppressLint("MissingPermission")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val bluetoothManager: BluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getSystemService(BluetoothManager::class.java)
//        } else {
//            TODO("VERSION.SDK_INT < M")
//        }
//        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
//
//        binding= ActivityDeviceListBinding.inflate(layoutInflater)
//        // Setup the window
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
//        setContentView(binding.root)
//
//
//
//        // Set result CANCELED inCase the user backs out
//        setResult(RESULT_CANCELED)
//
//        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
//            when(ContextCompat.checkSelfPermission(
//                baseContext,android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ))
//            {
//                PackageManager.PERMISSION_DENIED -> androidx.appcompat.app.AlertDialog.Builder(this)
//                    .setTitle("RunTime permission")
//                    .setMessage("Give permission")
//                    .setNeutralButton("Okay", DialogInterface.OnClickListener { dialog, which ->
//                        if (ContextCompat.checkSelfPermission(baseContext,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
//                            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(baseContext,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
//                            PackageManager.PERMISSION_GRANTED ){
//                            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION
//                                                                ,android.Manifest.permission.ACCESS_FINE_LOCATION),101)
//                        }
//                    }).show()
//                //.findViewById<TextView>(R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()
//
//                PackageManager.PERMISSION_GRANTED -> {
//                    Log.d("DiscoverDevice","Permission Granted")
//                }
//            }
//        }
//
//        binding.buttonScan.setOnClickListener {
//            doDiscovery(bluetoothAdapter!!)
//            it.visibility = View.GONE
//        }
//
//
//        // Initialize array adapters. One for already paired devices and
//        // one for newly discovered devices
//        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
//        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
//
//        // Find and set up the ListView for paired devices
////        val pairedListView = findViewById<ListView>(R.id.paired_devices)
////        pairedListView.adapter = mPairedDevicesArrayAdapter
////        pairedListView.onItemClickListener = mDeviceClickListener
//
//
//        // Find and set up the ListView for newly discovered devices
//        val newDevicesListView = findViewById<ListView>(R.id.new_devices)
//        newDevicesListView.adapter = mNewDevicesArrayAdapter
////        newDevicesListView.onItemClickListener = mDeviceClickListener
//
//
//        // Register for broadcasts when a device is discovered
//        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        this.registerReceiver(mReceiver, filter)
//
//
//        // Register for broadcasts when discovery has finished
//        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        this.registerReceiver(mReceiver, filter)
//
//        // Get a set of currently paired devices
//
//        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
//
//        val list=ArrayList<BluetoothDevice>()
//        bluetoothAdapter!!.bondedDevices.forEach() {
//            list.add(it)
//        }
//
//        deviceAdapter=DeviceAdapter(list){
//            Toast.makeText(applicationContext,it.name.toString(),Toast.LENGTH_LONG).show()
////            it.createBond()
////            it.createRfcommSocketToServiceRecord(it.uuids[0].uuid).connect()
//            connect(it)
//        }
//
//        binding.pairedDevices.adapter=deviceAdapter
//
//        // If there are paired devices, add each one to the ArrayAdapter
//        if (pairedDevices.isNotEmpty()) {
//            binding.titlePairedDevices.visibility = View.VISIBLE
//            for (device in pairedDevices) {
//                mPairedDevicesArrayAdapter!!.add(
//                    """
//                ${device.name}
//                ${device.address}
//                """.trimIndent()
//                )
//            }
//        } else {
//            val noDevices = resources.getText(R.string.none_paired).toString()
//            mPairedDevicesArrayAdapter!!.add(noDevices)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
////        if (mService != null) {
////            mService.cancelDiscovery()
////        }
////        mService = null
//        unregisterReceiver(mReceiver)
//    }
//
//
//    /**
//     * Start device discover with the BluetoothAdapter
//     */
//    @SuppressLint("MissingPermission")
//    private fun doDiscovery(bluetoothAdapter: BluetoothAdapter) {
//
//        // Indicate scanning in the title
//        setProgressBarIndeterminateVisibility(true)
//        setTitle(R.string.scanning)
//
//        // Turn on sub-title for new devices
//        binding.titleNewDevices.visibility = View.VISIBLE
//
//        val myPairedDevices = bluetoothAdapter.bondedDevices
//        val list : ArrayList<BluetoothDevice> = ArrayList()
//
//        if (myPairedDevices.isNotEmpty())
//        {
//            for ( device:BluetoothDevice in myPairedDevices)
//                list.add(device)
//
//            //list.add(device.name() + "\n" + device.address())
//
//            Log.i("Device", "This is message $list")
//
//        }
//        else {
//            Toast.makeText(applicationContext, " NO PAIRED DEVICES FOUND", Toast.LENGTH_LONG).show()
//
//        }
//
//        // If we're already discovering, stop it
//        if (bluetoothAdapter.isDiscovering) {
//            bluetoothAdapter.cancelDiscovery()
//        }
//
//        // Request discover from BluetoothAdapter
//        bluetoothAdapter.startDiscovery()
//    }
//
//
////    // The on-click listener for all devices in the ListViews
////    private val mDeviceClickListener =
////        OnItemClickListener { _, v, _, _ ->
////            //����б�������豸
////            // Cancel discovery because it's costly and we're about to connect
//////            mService.cancelDiscovery()
////
////            // Get the device MAC address, which is the last 17 chars in the View
////            val info = (v as TextView).text.toString()
////            val address = info.substring(info.length - 17)
////
////
////            // Create the result Intent and include the MAC address
////            val intent = Intent()
////            intent.putExtra(extraDeviceAddress, address)
////            Log.d("���ӵ�ַ", address)
////
////            // Set result and finish this Activity
////            setResult(RESULT_OK, intent)
////            finish()
////        }
//

//
//    // The BroadcastReceiver that listens for discovered devices and
//    // changes the title when discovery is finished
//    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND == action) {
//                // Get the BluetoothDevice object from the Intent
//                val device =
//                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                // If it's already paired, skip it, because it's been listed already
//                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
//                    mNewDevicesArrayAdapter!!.add(
//                        """
//                        ${device.name}
//                        ${device.address}
//                        """.trimIndent()
//                    )
//                }
//                // When discovery is finished, change the Activity title
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
//                setProgressBarIndeterminateVisibility(false)
//                setTitle(R.string.select_device)
//                if (mNewDevicesArrayAdapter!!.count == 0) {
//                    val noDevices = resources.getText(R.string.none_found).toString()
//                    mNewDevicesArrayAdapter!!.add(noDevices)
//                }
//            }
//        }
//    }
//}


/*
private val _permission = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    else -> arrayOf(Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
}

private val blueToothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionEntries ->
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> permissionEntries.entries.forEach {
            when {
                it.key == _permission[0] && it.value -> {}
                it.key == _permission[1] && it.value -> {}
                it.key == _permission[2] && it.value -> {}
                it.key == _permission[3] && it.value -> {}
                it.key == _permission[4] && it.value -> {}
            }
        }
        else -> permissionEntries.entries.forEach {
            when {
                it.key == _permission[0] && it.value -> {}
                it.key == _permission[1] && it.value -> {}
                it.key == _permission[2] && it.value -> {}
            }
        }
    }
}*/
