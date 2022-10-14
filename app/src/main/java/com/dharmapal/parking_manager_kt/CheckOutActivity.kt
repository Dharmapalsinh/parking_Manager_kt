package com.dharmapal.parking_manager_kt

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityCheckOutBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException

class CheckOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckOutBinding
    private lateinit var viewmodel: MainViewmodel

    private var cameraSource: CameraSource? = null
    val RequestCameraPermissionID = 1001
    var codes: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        HomeActivity.callNetworkConnection(application,this,this,viewmodel)
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.w("MainActivity", "Detector dependencies are not yet available.")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()

            val sfhTrackHolder = binding.surfaceView.holder
            sfhTrackHolder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@CheckOutActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                RequestCameraPermissionID
                            )
                            return
                        }
                        cameraSource!!.start(binding.surfaceView!!.holder)
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
                    cameraSource!!.stop()
                }
            })
        }

        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<TextBlock>) {
                val items = detections.detectedItems
                if (items.size() != 0) {
                   binding.cameraTxt.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }
                        binding.cameraTxt.text = stringBuilder.toString()
                    }
                }
            }
        })

       binding.capture.setOnClickListener {
           if (HomeActivity.checkForInternet(this)){
               codes = binding.cameraTxt.text.toString().trim { it <= ' ' }
               checkout(codes!!)
           }
           else{
               HomeActivity.NetworkDialog(this,viewmodel)
           }
       }
    }

    private fun checkout(codes: String) {
        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",codes)
        val numPlate:Regex =
            "^[A-Z]{1,2}\\s?[0-9]{1,2}\\s?[A-Z]{1,2}\\s?[0-9]{4}\$".toRegex()


        if (codes.contains(numPlate)){

            viewmodel.missing(codes)
            showMe.dismiss()
            viewmodel.missingData.observe(this){
                Log.d("missing", it.msg +codes)
                Toast.makeText(applicationContext,it.msg,Toast.LENGTH_SHORT).show()
            }
            viewmodel.errorMessage.observe(this){
                Log.d("missing",it.toString())
            }
        }
        else{
                            Toast.makeText(applicationContext,"try again",Toast.LENGTH_SHORT).show()
        }

        showMe.dismiss()
    }

}