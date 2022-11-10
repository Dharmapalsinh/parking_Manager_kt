package com.dharmapal.parking_manager_kt

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.checkForInternet
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Utills.Config.Companion.permissionRequestCode
import com.dharmapal.parking_manager_kt.Utills.Config.Companion.requestCameraPermissionID
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
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Method


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    var smartCode: EditText? = null
    private lateinit var cameraSource: CameraSource
    var temp: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var pAdapter: PriceAdapter? = null
    private var objMediaPlayer: MediaPlayer? = null
    private var lottieAnimationView: LottieAnimationView? = null
    private val price: ArrayList<PriceModel> = ArrayList()
    private var pid = ""
    private var slots: String? = null
    private var slotId: String? = null
    private var pno: String? = null
    private var cardType: String? = "3"
    private var code: String? = null
    private var vTypes: String? = null
    private var types: String? = null
    private lateinit var bluetoothManager: BluetoothManager
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val outputStream: OutputStream? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var delay = 1000
    private lateinit var  textRecognizer:TextRecognizer

    @SuppressLint("MissingPermission")
    override fun onResume() {
        Log.d("lcd","resume")

        //Loop every 1 second
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            if (mBluetoothAdapter!!.bondedDevices.isNotEmpty()){
                val connectedDv= mBluetoothAdapter!!.bondedDevices.filter {
                    isConnected(it)
                }

                if (connectedDv.isNotEmpty()){
                    binding.dvName.text=connectedDv[0].name
                    binding.btnConnect.text=getString(R.string.change)
                }
                else{
                    binding.dvName.text=getString(R.string.Nodevice)
                    binding.btnConnect.text=getString(R.string.connect)
                }
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
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibBackButton.setOnClickListener {
            finish()
        }

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

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
            cardType = "2"

            if (binding.vnumber.text.toString() == "") {
                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
            } else if (pid == "") {
                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
            } else {
                bottomSheet(pid, slots, slotId, pno, cardType)
            }

            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }

        binding.vipcard.setOnClickListener {
            cardType = "1"
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }

        binding.normalcard.setOnClickListener {
            cardType = "3"
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
        }

        binding.btnConnect.setOnClickListener {
            val intent=Intent(applicationContext,DeviceListActivity::class.java)
            startActivity(intent)
        }

        textRecognizer = TextRecognizer.Builder(applicationContext).build()

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
                            "^[A-Z]{2}\\s?[0-9]{2}\\s?[A-Z]{1,2}\\s?[0-9]{4}\$".toRegex()


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
//                            binding.constSurface.layoutParams =
//                                ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, 280)

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

            if (binding.vnumber.text.toString() == "") {
                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
            } else if (pid == "") {
                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
            }
            //todo:below condition
//            else if (binding.dvName.text == getString(R.string.Nodevice)) {
//                Toast.makeText(this@MainActivity, "Please Select Bluetooth Device!!",Toast.LENGTH_SHORT).show()
//            }
            else {

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

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestCode->{
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(applicationContext, "denied", Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                                    "package:$packageName"
                                )
                            )
                        )
                    }
                    else if (element==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(applicationContext, "granted", Toast.LENGTH_LONG).show()
                        cameraSource.start(binding.surfaceView.holder)
                    }
                }
            }
        }
    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
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


    private fun getPrintFormat(
        passNo: String, datetime: String, vno: String, vType: String, slotNo: String,
        type: String, amount: String): String {

        val builder = java.lang.StringBuilder()
        builder.append("!!Spotiz-Parking!!\n")
        builder.append("\n")
        builder.append("Date & Time: $datetime")
        builder.append("\n")
        builder.append("Vehicle No : $vno")
        builder.append("\n")
        builder.append("--------------------------------")
        builder.append("\n")
        builder.append("Pass No : $passNo")
        builder.append("\n")
        builder.append("Parking Slot No : $slotNo")
        builder.append("\n")
        builder.append("--------------------------------")
        builder.append("\n")
        when (vType) {
            "2" -> {
                vTypes="Two Wheeler"
            }
            "3" -> {
                vTypes="Three Wheeler"
            }
            "4"->{
                vTypes="Four Wheeler"
            }
        }
        builder.append("Vehicle Type : $vTypes ")
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

    @SuppressLint("MissingPermission")
    private fun checkInPrint()
    {

        lists()
        viewModel.save(SaveParameters(binding.vnumber.text.toString().replace("\\s".toRegex(),"").uppercase(),pid,slots!!,slotId!!,cardType!!,"0"))
        viewModel.saveData.observe(this){
            //todo:animation below
//            if (it.status=="200"){
//                binding.animationview.isVisible = true
//                binding.animationview.playAnimation()
//            }
            Log.d("save",it.toString())
            Toast.makeText(applicationContext, it.msg,Toast.LENGTH_SHORT).show()

            val cmd = ByteArray(3)
            cmd[0] = 0x1b
            cmd[1] = 'a'.code.toByte()
            cmd[2] = 0x01
            outputStream?.write(cmd)
            outputStream?.write(
                getPrintFormat(
                    it.pass_no,
                    it.checked_in,
                    it.vehicle_no,
                    it.vehicle_type,
                    it.slot_number,
                    it.type,
                    it.amount
                ).toByteArray())

            Log.d("print",
                getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)
            )

            val imageCmd = ByteArray(7)
            imageCmd[0] = 0x1B
            imageCmd[1] = 0x5A
            imageCmd[2] = 0x00
            imageCmd[3] = 0x02
            imageCmd[4] = 0x07
            imageCmd[5] = 0x06
            imageCmd[6] = 0x00
            outputStream?.write(imageCmd)
            outputStream?.write(("" + it.pass_no).toByteArray())

            Log.d("print", "" + it.pass_no)
           //mService.sendMessage("" + it.pass_no, "GBK")

            binding.vnumber.text.clear()
            binding.slotNo.text = "-"
            cameraSource.start(binding.surfaceView.holder)
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            binding.normalcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)

        }
    }

        private fun bottomSheet(
            pid: String?,
            slots: String?,
            slotsId: String?,
            cardTypes: String?,
            vno: String?
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
    }

    @SuppressLint("MissingPermission")
    fun getData(pID: String, slot: String, slotsId: String, cardType: String, codes: String, vno: String) {

            lists()
            viewModel.save(SaveParameters(vno,pID,slot,slotsId,cardType,codes))
            viewModel.saveData.observe(this){
                Log.d("save",it.toString())

                val cmd = ByteArray(3)
                cmd[0] = 0x1b
                cmd[1] = 'a'.code.toByte()
                cmd[2] = 0x01
                outputStream?.write(cmd)
                outputStream?.write(
                    getPrintFormat(
                        it.pass_no,
                        it.checked_in,
                        it.vehicle_no,
                        it.vehicle_type,
                        it.slot_number,
                        it.type,
                        it.amount
                    ).toByteArray())

                Log.d("print",
                    getPrintFormat(it.pass_no,it.checked_in,it.vehicle_no, it.vehicle_type,it.slot_number,it.type,it.amount)
                )

                val imageCmd = ByteArray(7)
                imageCmd[0] = 0x1B
                imageCmd[1] = 0x5A
                imageCmd[2] = 0x00
                imageCmd[3] = 0x02
                imageCmd[4] = 0x07
                imageCmd[5] = 0x06
                imageCmd[6] = 0x00
                outputStream?.write(imageCmd)
                outputStream?.write(("" + it.pass_no).toByteArray())

                Log.d("print", "" + it.pass_no)
                //mService.sendMessage("" + it.pass_no, "GBK")

                binding.vnumber.text.clear()
                binding.slotNo.text = "-"
                cameraSource.start(binding.surfaceView.holder)
                binding.prepaidcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
                binding.vipcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
                binding.normalcard.background =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
            }
    }

    private fun getSlot(pid: String) {
        viewModel.slot((pid))
        viewModel.slotData.observe(this){
            Log.d("slot",it.toString()+pid)
            slots=it.slot
            slotId=it.slot_id
            binding.slotNo.text = it.slot
        }
    }
    //todo: remove unwanted permissions in manifest
}


