package com.dharmapal.parking_manager_kt

import android.Manifest
import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Printer
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.checkForInternet
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.adapters.PriceAdapter
import com.dharmapal.parking_manager_kt.databinding.ActivityMainBinding
import com.dharmapal.parking_manager_kt.models.PriceModel
import com.dharmapal.parking_manager_kt.models.SaveParameters
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    var smartCode: EditText? = null
    private lateinit var cameraSource: CameraSource
    val requestCameraPermissionID = 1001
    var temp: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var pAdapter: PriceAdapter? = null
    private var objMediaPlayer: MediaPlayer? = null
    private var lottieAnimationView: LottieAnimationView? = null
    private val price: ArrayList<PriceModel> = ArrayList()
    private var pid = ""
    private var slots: String? = null
    private var slotid: String? = null
    private var pno: String? = null
    private var cardtype: String? = "3"
    var code: String? = null
    private val permissionRequestCode = 200
    private val requestConnectDevice = 1
    var vtypes: String? = null
    var types: String? = null
    private lateinit var bluetoothManager: BluetoothManager
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private val outputStream: OutputStream? = null
    private val inStream: InputStream? = null

    override fun onStart() {
        super.onStart()
        Log.d("lcycle","start")
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
                bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                mBluetoothAdapter = bluetoothManager.adapter
                if (mBluetoothAdapter!!.bondedDevices.isNotEmpty()){
                    val connected_dv= mBluetoothAdapter!!.bondedDevices.filter {
                        it.bondState==BluetoothDevice.BOND_BONDED
                    }
                    if (connected_dv.isNotEmpty()){
                        binding.dvName.text=connected_dv[0].name
                    }
                    else{
                        binding.dvName.text="No Device Connected"
                    }
                }
            }
        }
        else{
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothAdapter = bluetoothManager.adapter
            if (mBluetoothAdapter!!.bondedDevices.isNotEmpty()){
                val connected_dv= mBluetoothAdapter!!.bondedDevices.filter {
                    it.bondState==BluetoothDevice.BOND_BONDED
                }
                if (connected_dv.isNotEmpty()){
                    binding.dvName.text=connected_dv[0].name
                }
                else{
                    binding.dvName.text="No Device Connected"
                }
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lcycle","create")
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

//        val serverIntent = Intent(applicationContext, DeviceListActivity::class.java)
//        startActivityForResult(serverIntent, requestConnectDevice)

        requestPermission()
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        if (mBluetoothAdapter == null) {
//            Log.d("tag", "enableDisableBT: Does not have BT capabilities.")
//        } else if (!mBluetoothAdapter!!.isEnabled) {
//            mBluetoothAdapter!!.enable()
//        }

        callNetworkConnection(application!!, this, this, viewModel)
        binding.recyclerView.layoutManager=
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        pAdapter= PriceAdapter(price){
            val model = it
            pid = model.id.toString()
            getSlot(pid)
        }
        binding.recyclerView.adapter=pAdapter

        binding.prepaidcard.setOnClickListener {
            cardtype = "2"

            if (binding.vnumber.text.toString() == "") {
                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
            } else if (pid == "") {
                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
            } else {
                bottomSheet(pid, slots, slotid, pno, cardtype,binding.vnumber.text.toString())
            }

            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }

        binding.vipcard.setOnClickListener {
            cardtype = "1"
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }

        binding.normalcard.setOnClickListener {
            cardtype = "3"
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }


        binding.btnCash.setOnClickListener {

            if (binding.btnUpi.isChecked){
                binding.btnUpi.isChecked=false
            }
        }

        binding.btnUpi.setOnClickListener {

            if (binding.btnCash.isChecked){
                binding.btnCash.isChecked=false
            }

            if (binding.btnUpi.isChecked){
                Toast.makeText(applicationContext,"new act",Toast.LENGTH_SHORT).show()
            }
            else{}
        }

        binding.btnConnect.setOnClickListener {
            val intent=Intent(applicationContext,DeviceListActivity::class.java)
            startActivity(intent)
        }

        val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.w("MainActivity", "Detector dependencies are not yet available.")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(400, 480)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()

            binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                permission.CAMERA
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(permission.CAMERA),
                                requestCameraPermissionID
                            )
                            return
                        }
                        cameraSource.start(binding.surfaceView.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    cameraSource.stop()
                }
            })
        }


        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<TextBlock?>) {
                val items: SparseArray<TextBlock?>? = detections.detectedItems
                if (items!!.size() != 0) {
                    binding.cameraTxt.post {
                        val stringBuilder = StringBuilder()

                        val numPlate:Regex =
                            "^[A-Z]{1,2}\\s?[0-9]{1,2}\\s?[A-Z]{1,2}\\s?[0-9]{4}\$".toRegex()


                            for (i in 0 until items.size()) {
                                val item: TextBlock = items.valueAt(i)!!
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }

                        Log.d("string",stringBuilder.toString())
                        if (stringBuilder.toString().contains(numPlate)){
                            binding.cameraTxt.text = stringBuilder.toString()
                            temp = binding.cameraTxt.text.toString().trim { it <= ' ' }
                            playOnOffSound()
                            binding.vnumber.setText(stringBuilder.toString().replace("\\s".toRegex(),""))
                            cameraSource.stop()
                        }
                        else{
//                            Toast.makeText(applicationContext,"try again",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        binding.capture.setOnClickListener {
            try {
                cameraSource.start(binding.surfaceView.holder)
                binding.vnumber.text.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.printPass.setOnClickListener {
            /*val outputStream:OutputStream? =null
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
//                    outputStream!!.write("First Testing".toByteArray())
                    doPrint()
                }
            }*/
//            checkInPrint()
//             Submit();
            if (binding.vnumber.text.toString() == "") {
                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
            } else if (pid == "") {
                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
            } else {

                if(checkForInternet(this)){
                    checkInPrint()
                }
                else{
                    networkDialog()
                }
            }
        }

        lists()
    }

    private fun doPrint() {

            // Get a PrintManager instance
            val printManager = this.getSystemService(Context.PRINT_SERVICE) as PrintManager
            // Set job name, which will be displayed in the print queue
            val jobName = "${this.getString(R.string.app_name)} Document"
            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document
            Log.d("printwifi","print")
            printManager.print(jobName, MyPrintDocumentAdapter(this), null)

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==103 ){
            for (element in grantResults) {
                if (element == PackageManager.PERMISSION_GRANTED) {
                    bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    mBluetoothAdapter = bluetoothManager.adapter
                    if (mBluetoothAdapter!!.bondedDevices.isNotEmpty()){
                        val connected_dv= mBluetoothAdapter!!.bondedDevices.filter {
                            it.bondState==BluetoothDevice.BOND_BONDED
                        }
                        if (connected_dv.isNotEmpty()){
                            binding.dvName.text=connected_dv[0].name
                        }
                        else{
                            binding.dvName.text="No Device Connected"
                        }
                    }
                }
            }
        }
    }
    private fun playOnOffSound() {
        objMediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.beep)
        objMediaPlayer!!.setOnCompletionListener { mp -> mp.release() }
        objMediaPlayer!!.start()
    }

    private fun networkDialog(){
        val  dialogs = Dialog(this@MainActivity )
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done: Button = dialogs.findViewById<View>(R.id.done) as Button
        done.setOnClickListener {
            dialogs.dismiss()
            lists()
            checkInPrint()
        }
        dialogs.show()
    }


    fun getPrintFormat(
        passno: String, datetime: String, vno: String, vtype: String, slotno: String,
        type: String, amount: String): String? {

        val builder = java.lang.StringBuilder()
        builder.append("!!Spotiz-Parking!!\n")
        builder.append("\n")
        builder.append("Date & Time: $datetime")
        builder.append("\n")
        builder.append("Vehicle No : $vno")
        builder.append("\n")
        builder.append("--------------------------------")
        builder.append("\n")
        builder.append("Pass No : $passno")
        builder.append("\n")
        builder.append("Parking Slot No : $slotno")
        builder.append("\n")
        builder.append("--------------------------------")
        builder.append("\n")
        when (vtype) {
            "2" -> {
                vtypes="Two Wheeler"
            }
            "3" -> {
                vtypes="Three Wheeler"
            }
            "4"->{
                vtypes="Four Wheeler"
            }
        }
        builder.append("Vehicle Type : $vtypes ")
        builder.append("\n")
        when (type) {
            "1" -> {
                types="VIP User"
            }
            "2" -> {
                types="Prepaid User"
            }
            "3"->{
                types="Normal User"
            }
        }
        builder.append("Customer Type : $types")
        builder.append("\n")
        builder.append("--------------------------------\n")
        builder.append("Amount Paid : $amount")
        builder.append("\n")
        builder.append("\n")
        return builder.toString()
    }

    private fun checkInPrint()
    {

        val showMe = ProgressDialog(this@MainActivity)
        showMe.setMessage("Please wait")
        showMe.setCancelable(false)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()


        lists()
        viewModel.save(SaveParameters(binding.vnumber.text.toString(),pid,slots!!,slotid!!,cardtype!!,"0"))
        viewModel.saveData.observe(this){
            Log.d("save",it.toString())
            Toast.makeText(applicationContext, it.msg,Toast.LENGTH_SHORT).show()

            val cmd = ByteArray(3)
            cmd[0] = 0x1b
            cmd[1] = 'a'.code.toByte()
            cmd[2] = 0x01
            outputStream?.write(cmd)
            outputStream?.write(getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)!!.toByteArray())

            Log.d("print","${getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)}")

            val imagecmd = ByteArray(7)
            imagecmd[0] = 0x1B
            imagecmd[1] = 0x5A
            imagecmd[2] = 0x00
            imagecmd[3] = 0x02
            imagecmd[4] = 0x07
            imagecmd[5] = 0x06
            imagecmd[6] = 0x00
            outputStream?.write(imagecmd)
            outputStream?.write(("" + it.pass_no).toByteArray())

            Log.d("print","${"" + it.pass_no}")
           //mService.sendMessage("" + it.pass_no, "GBK")

            binding.vnumber.text.clear()
            binding.slotNo.text = "-"
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)

            try {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@observe
                }
                cameraSource.start(binding.surfaceView.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        showMe.dismiss()
    }

        private fun bottomSheet(
            pid: String?,
            slots: String?,
            slotsId: String?,
            cardTypes: String?,
            vno: String?,
            toString: String
        ) {
        bottomSheetDialog =
            BottomSheetDialog(this@MainActivity, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.bottomsheet)
        bottomSheetDialog!!.window!!.setBackgroundDrawable(ColorDrawable())
        bottomSheetDialog!!.setCanceledOnTouchOutside(false)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.show()
        lottieAnimationView =
            bottomSheetDialog!!.findViewById(R.id.animation_view) as LottieAnimationView?
        lottieAnimationView!!.playAnimation()
            lottieAnimationView!!.speed = 1.3.toFloat()
        smartCode = bottomSheetDialog!!.findViewById(R.id.smartcode)
        smartCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 10) {
                    code = smartCode!!.text.toString()
                    getData(pid!!, slots!!, slotsId!!, cardTypes!!, s.toString(), vno!!)
                    smartCode!!.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun lists()
    {
        //TODO:call api price
        val showMe = ProgressDialog(this@MainActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        viewModel.price()
        viewModel.priceData.observe(this){ priceResponse ->
            if (price.isEmpty()){
            priceResponse.twoWheeler!!.forEach {
                price.add(PriceModel(id = it.id, amount = it.price,type = it.vehicleType))

            }
            }
            pAdapter=PriceAdapter(price){
                val model = it
                pid = model.id.toString()
                getSlot(pid)
            }
            binding.recyclerView.adapter=pAdapter
        }

        showMe.dismiss()

    }

    fun getData(pID: String, slot: String, slotsId: String, cardType: String, codes: String, vno: String) {

        val showMe = ProgressDialog(this@MainActivity)
        showMe.setMessage("Please wait")
        showMe.setCancelable(false)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

            lists()
            viewModel.save(SaveParameters(vno,pID,slot,slotsId,cardType,codes))
            viewModel.saveData.observe(this){
                Log.d("save",it.toString())

                val cmd = ByteArray(3)
                cmd[0] = 0x1b
                cmd[1] = 'a'.code.toByte()
                cmd[2] = 0x01
                outputStream?.write(cmd)
                outputStream?.write(getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)!!.toByteArray())

                Log.d("print","${getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)}")

                val imagecmd = ByteArray(7)
                imagecmd[0] = 0x1B
                imagecmd[1] = 0x5A
                imagecmd[2] = 0x00
                imagecmd[3] = 0x02
                imagecmd[4] = 0x07
                imagecmd[5] = 0x06
                imagecmd[6] = 0x00
                outputStream?.write(imagecmd)
                outputStream?.write(("" + it.pass_no).toByteArray())

                Log.d("print","${"" + it.pass_no}")
                //mService.sendMessage("" + it.pass_no, "GBK")

                binding.vnumber.text.clear()
                binding.slotNo.text = "-"
                binding.prepaidcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
                binding.vipcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
                binding.normalcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)

                /* try {
                     if (ActivityCompat.checkSelfPermission(
                             this@MainActivity,
                             permission.CAMERA
                         ) != PackageManager.PERMISSION_GRANTED
                     ) {
                         return@observe
                     }
                     cameraSource.start(binding.surfaceView.holder)
                 } catch (e: IOException) {
                     e.printStackTrace()
                 }*/
            }

            showMe.dismiss()
    }

    private fun getSlot(pid: String) {
        val showMe = ProgressDialog(this@MainActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()


        viewModel.slot((pid))
        viewModel.slotData.observe(this){
            Log.d("slot",it.toString()+pid)
            slots=it.slot
            slotid=it.slot_id
            binding.slotNo.text = it.slot
        }

        showMe.dismiss()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.CAMERA,
                permission.WRITE_EXTERNAL_STORAGE,
                permission.READ_EXTERNAL_STORAGE,
                permission.BLUETOOTH
            ),
            permissionRequestCode
        )


    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(applicationContext,HomeActivity::class.java))
//        finish()
    }

}


