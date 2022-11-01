

package com.dharmapal.parking_manager_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.networkDialog
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Utills.Config.scan
import com.dharmapal.parking_manager_kt.databinding.ActivityQrCodeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewModel: MainViewModel
    private var vNumber: TextView? = null
    private  var missing:TextView? = null
    private var pass: EditText? = null
    private var objMediaPlayer: MediaPlayer? = null
    private var checkout: CardView? = null
    private var lay: RelativeLayout? = null
//    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

//        detector=BarcodeDetector.Builder(this).build()

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

            binding.surfaceview.holder.addCallback(surfaceCallback)
        }
//        detector.setProcessor(processor)

//        lay = findViewById(R.id.relVhcle)

        vNumber = findViewById(R.id.vnumber)
//        missing = findViewById(R.id.missingpass1)
//        pass = findViewById(R.id.passnoo)
        checkout = findViewById(R.id.btn_checkout)

        callNetworkConnection(application!!, this, this, viewModel)

        binding.btnCheckout.setOnClickListener{
            if(HomeActivity.checkForInternet(this)){
                checkout(binding.vNumber.text.toString())
            }
            else{
                networkDialog(this,viewModel)
            }

        }
//       binding.missingpass1.setOnClickListener {
//            val i = Intent(this, CheckOutActivity::class.java)
//            startActivity(i)
//
//       }


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
                            val temp = binding.cameraTxt.text.toString().trim { it <= ' ' }
                            playOnOffSound()
                            binding.vNumber.setText(stringBuilder.toString().trim { it <= ' ' })
                            viewModel.arrivingVehicle(binding.vNumber.text.toString())
                            viewModel.arrivingVehicleData.observe(this@QrCodeActivity){
                                binding.arrTime.text=it.response!!.checkinTime
                            }
                            cameraSource.stop()
                        }
                        else{
//                            Toast.makeText(applicationContext,"try again",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

    }

    private fun playOnOffSound() {
        objMediaPlayer = MediaPlayer.create(this@QrCodeActivity, R.raw.beep)
        objMediaPlayer!!.setOnCompletionListener { mp -> mp.release() }
        objMediaPlayer!!.start()
    }

    private val surfaceCallback= object :SurfaceHolder.Callback{
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(p0: SurfaceHolder) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@QrCodeActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        1001
                    )
                    return
                }
                cameraSource.start(binding.surfaceview.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {

        }
    }


    fun scan(result: String) {
//        val showMe = ProgressDialog(this@QrCodeActivity, AlertDialog.THEME_HOLO_LIGHT)
//        showMe.setMessage("Please wait")
//        showMe.setCancelable(true)
//        showMe.setCanceledOnTouchOutside(false)
//        showMe.show()

        Log.d("tagged",result)
        viewModel.scan(result)
//        showMe.dismiss()
        viewModel.scanData.observe(this){
            Log.d("scan", it.msg)
        }
        viewModel.errorMessage.observe(this){
            Log.d("scan",it.toString())
        }
    }

    private fun checkout(result: String) {
        val showMe = ProgressDialog(this@QrCodeActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",result)
        viewModel.checkout(result)
        showMe.dismiss()
        viewModel.checkoutData.observe(this){
            Log.d("checkout", it.msg)
            Toast.makeText(applicationContext,it.msg,Toast.LENGTH_SHORT).show()
        }
        viewModel.errorMessage.observe(this){
            Log.d("checkout",it.toString())
        }
    }

}