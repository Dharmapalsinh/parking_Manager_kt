package com.dharmapal.parking_manager_kt

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
import java.io.IOException

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
    private var mBluetoothAdapter: BluetoothAdapter? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val serverIntent = Intent(applicationContext, DeviceListActivity::class.java)
        startActivityForResult(serverIntent, requestConnectDevice)

        requestPermission()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBluetoothAdapter == null) {
            Log.d("tag", "enableDisableBT: Does not have BT capabilities.")
        } else if (!mBluetoothAdapter!!.isEnabled) {
            mBluetoothAdapter!!.enable()
        }

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
        }

        binding.vipcard.setOnClickListener {
            cardtype = "1"
            binding.vipcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            binding.prepaidcard.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)

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
                            binding.vnumber.setText(stringBuilder.toString().trim { it <= ' ' })
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


//            checkInPrint()
//            // Submit();
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


    private fun checkInPrint()
    {

        val showMe = ProgressDialog(this@MainActivity)
        showMe.setMessage("Please wait")
        showMe.setCancelable(false)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        //TODO:call api save

        lists()
        viewModel.save(SaveParameters(binding.vnumber.text.toString(),pid,slots!!,slotid!!,cardtype!!,"0"))
        viewModel.saveData.observe(this){
            Log.d("save",it.toString())
            Toast.makeText(applicationContext, it.msg,Toast.LENGTH_SHORT).show()
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

        //TODO:call api price

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
            }

            showMe.dismiss()
    }

    fun getSlot(pid: String) {
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


