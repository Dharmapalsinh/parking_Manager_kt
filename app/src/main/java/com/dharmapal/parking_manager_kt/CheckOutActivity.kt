package com.dharmapal.parking_manager_kt

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dharmapal.parking_manager_kt.databinding.ActivityCheckOutBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
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
    private val surfaceView: SurfaceView? = null
 //   private val sessionManager: SessionManager? = null
   /* var stringRequest: StringRequest? = null
    var stringRequest1:StringRequest? = null
    var mRequestQueue: RequestQueue? = null
    var mRequestQueue1:RequestQueue? = null*/
    val TAG = "STag"
    var codes: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

            val sfhTrackHolder = binding.surfaceView!!.holder
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
                        cameraSource!!.start(surfaceView!!.holder)
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
                   binding.cameraTxt!!.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }
                        binding.cameraTxt!!.text = stringBuilder.toString()
                    }
                }
            }
        })

       binding.capture.setOnClickListener(View.OnClickListener {
            codes = binding.cameraTxt!!.text.toString().trim { it <= ' ' }
            Checkout(codes!!)
        })
    }

    private fun Checkout(codes: String) {


    }

    private fun NetworkDialogs(id: String) {
        val dialogs = Dialog(this)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done = dialogs.findViewById<View>(R.id.done) as Button
        done.setOnClickListener {
            dialogs.dismiss()
            Checkout(id)
        }
        dialogs.show()
    }
}