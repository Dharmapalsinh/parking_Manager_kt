package com.dharmapal.parking_manager_kt

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.adapters.PriceAdapter
import com.dharmapal.parking_manager_kt.databinding.ActivityMainBinding
import com.dharmapal.parking_manager_kt.models.PriceModel
import com.dharmapal.parking_manager_kt.models.SaveParameters
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewmodel: MainViewmodel
    private val surfaceView: SurfaceView? = null
    var sfhTrackHolder: SurfaceHolder? = null
    private val textView: TextView? = null
    private  var slot:TextView? = null
    private  var passno:TextView? = null
    private  var print:TextView? = null
    var smartcode: EditText? = null
    var vnumber:EditText? = null
    private val prepaid: RelativeLayout? = null
    private  var vip:RelativeLayout? = null
    private lateinit var cameraSource: CameraSource
    val RequestCameraPermissionID = 1001
    private val capture: ImageView? = null
    var codes: String? = null
    var temp:kotlin.String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    var recyclerView: RecyclerView? = null
    var pAdapter: PriceAdapter? = null
    private var objMediaPlayer: MediaPlayer? = null
    var lottieAnimationView: LottieAnimationView? = null
//    private val sessionManager: SessionManager? = null
//    var stringRequest: StringRequest? = null
//    var stringRequest1:StringRequest? = null
//    var mRequestQueue: RequestQueue? = null
//    var mRequestQueue1:RequestQueue? = null
    private val price: ArrayList<PriceModel> = ArrayList()
    val TAG = "STag"
    var pid = ""
    var slots:kotlin.String? = null
    var slotid:kotlin.String? = null
    var pno:kotlin.String? = null
    var cardtype:kotlin.String? = "3"
    var code:kotlin.String? = null
    var showingFirst = true
    private val PERMISSION_REQUEST_CODE = 200
    private val REQUEST_CONNECT_DEVICE = 1
//    var mService: BluetoothService? = null
    var con_dev: BluetoothDevice? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    var vtypes: String? = null
    var types:kotlin.String? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val serverIntent = Intent(applicationContext, DeviceListActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)

        requestPermission()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBluetoothAdapter == null) {
            Log.d("tag", "enableDisableBT: Does not have BT capabilities.")
        } else if (!mBluetoothAdapter!!.isEnabled) {
            mBluetoothAdapter!!.enable()
        }

        binding.recyclerView.layoutManager=
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        pAdapter= PriceAdapter(applicationContext,price)
        binding.recyclerView.adapter=pAdapter

//        binding.recyclerView.addOnItemTouchListener

        binding.prepaidcard.setOnClickListener {
            cardtype = "2"

            if (vnumber!!.text.toString() == "") {
                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
            } else if (pid == "") {
                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
            } else {
                BottomSheet(pid, slots, slotid, pno, cardtype, vnumber!!.text.toString())
            }

            prepaid!!.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            vip!!.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)
        }

        binding.vipcard.setOnClickListener {
            cardtype = "1"
            vip!!.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.selectedbordercategory)
            prepaid!!.background =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.bordercategory)

        }

        val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available.")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
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
                                RequestCameraPermissionID
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
                val items: SparseArray<TextBlock?>? = detections.getDetectedItems()
                if (items!!.size() != 0) {
                    binding.cameraTxt.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item: TextBlock = items.valueAt(i)!!
                            stringBuilder.append(item.getValue())
                            stringBuilder.append("\n")
                        }
                        binding.cameraTxt.text = stringBuilder.toString()
                        temp = binding.cameraTxt.text.toString().trim { it <= ' ' }
                        playOnOffSound()
                        binding.vnumber.setText(stringBuilder.toString().trim { it <= ' ' })
                        cameraSource.stop()
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


            checkinprint()
//            // Submit();
//            if (vnumber!!.text.toString() == "") {
//                Toast.makeText(this@MainActivity, "Please Scan Vehicle or Enter Vehicle Number!!!",Toast.LENGTH_SHORT).show()
//            } else if (pid == "") {
//                Toast.makeText(this@MainActivity, "Please Select Vehicle Type!!!",Toast.LENGTH_SHORT).show()
//            } else {
//                checkinprint()
//            }
        }

        Lists()
    }

    private fun playOnOffSound() {
        objMediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.beep)
        objMediaPlayer!!.setOnCompletionListener { mp -> mp.release() }
        objMediaPlayer!!.start()
    }

    private fun NetworkDialog(){
        val  dialogs: android.app.Dialog = android.app.Dialog(this@MainActivity )
        dialogs.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done: android.widget.Button = dialogs.findViewById<android.view.View>(R.id.done) as android.widget.Button
        done.setOnClickListener(object : android.view.View.OnClickListener{
            override fun onClick(view: android.view.View){
                dialogs.dismiss()
                Lists()
                checkinprint()
            }
        })
        dialogs.show()
    }
    fun checkinprint()
    {

        val showMe = ProgressDialog(this@MainActivity)
        showMe.setMessage("Please wait")
        showMe.setCancelable(false)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Lists()
        viewmodel.save(SaveParameters("5555","TWO","111","2","T","0"))
        viewmodel.saveData.observe(this){
            Log.d("save",it.toString())
        }

        showMe.dismiss()
    }

        fun BottomSheet(
        pid: String?,
        slots: String?,
        slotsid: String?,
        pno: String?,
        cardtypess: String?,
        vno: String?
    ) {
        bottomSheetDialog =
            BottomSheetDialog(this@MainActivity, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.bottomsheet)
        bottomSheetDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable())
        bottomSheetDialog!!.setCanceledOnTouchOutside(false)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.show()
        lottieAnimationView =
            bottomSheetDialog!!.findViewById(R.id.animation_view) as LottieAnimationView?
        lottieAnimationView!!.playAnimation()
        lottieAnimationView!!.setSpeed(1.3.toFloat())
        smartcode = bottomSheetDialog!!.findViewById(R.id.smartcode)
        smartcode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 10) {
                    code = smartcode!!.text.toString()
                    GetData(pid!!, slots!!, slotsid!!, cardtypess!!, s.toString(), vno!!)
                    smartcode!!.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun Lists()
    {

        val showMe = ProgressDialog(this@MainActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        viewmodel.price()
        viewmodel.priceData.observe(this){ priceResponse ->
            priceResponse.twoWheeler!!.forEach {
                price.add(PriceModel(id = it.id, amount = it.price,type = it.vehicleType))

            }
            pAdapter=PriceAdapter(applicationContext,price)
            binding.recyclerView.adapter=pAdapter
        }

        showMe.dismiss()
    }
        fun GetData(
        pids: String,
        slotss: String,
        slotsids: String,
        cardtypess: String,
        codes: String,
        vno: String
    ) {
        val showMe = ProgressDialog(this@MainActivity)
        showMe.setMessage("Please wait")
        showMe.setCancelable(false)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

            Lists()
            viewmodel.save(SaveParameters(vno,pids,slotss,slotsids,cardtypess,codes))
            viewmodel.saveData.observe(this){
                Log.d("save",it.toString())
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
            PERMISSION_REQUEST_CODE
        )
    }

}