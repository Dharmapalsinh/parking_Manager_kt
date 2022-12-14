

package com.dharmapal.parking_manager_kt

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.networkDialog
import com.dharmapal.parking_manager_kt.MainActivity.Companion.decreaseViewSize
import com.dharmapal.parking_manager_kt.MainActivity.Companion.increaseViewSize
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Utills.Config
import com.dharmapal.parking_manager_kt.Utills.Config.Companion.requestCameraPermissionID
import com.dharmapal.parking_manager_kt.databinding.ActivityQrCodeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewModel: MainViewModel
    private var vNumber: TextView? = null
    private var objMediaPlayer: MediaPlayer? = null
    private var checkout: CardView? = null
    private lateinit var cameraSource: CameraSource
    @SuppressLint("SimpleDateFormat")
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("hh:mm a")


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibBack.setOnClickListener {
            finish()
        }

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

        binding.capture.setOnClickListener {
            try {

                cameraSource.start(binding.surfaceview.holder)
                binding.vNumber.text.clear()
                increaseViewSize(binding.constSurfaceView)
                binding.capture.isVisible = false

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        vNumber = findViewById(R.id.vnumber)

        checkout = findViewById(R.id.btn_checkout)

        callNetworkConnection(application!!, this, this, viewModel)

        viewModel.checkoutData.observe(this@QrCodeActivity) {
            if (it.status == "200") {
                lifecycleScope.launch {
                    Log.d("checkout", it.msg)
                    val animation = binding.animationSubmit
                    binding.animationSubmit.isVisible = true
                    animation.playAnimation()

                    Toast.makeText(applicationContext, it.msg, Toast.LENGTH_SHORT).show()

                    binding.vNumber.text.clear()
                    binding.arrTime.text = ""
                    increaseViewSize(binding.constSurfaceView)
                    cameraSource.start(binding.surfaceview.holder)

                    delay(2000)
                    animation.cancelAnimation()
                    binding.animationSubmit.isVisible = false
                }
            } else {

                lifecycleScope.launch {
                    val animation = binding.animationInvalid
                    binding.animationInvalid.isVisible = true
                    animation.playAnimation()

                    Toast.makeText(applicationContext, it.msg, Toast.LENGTH_SHORT).show()

                    binding.vNumber.text.clear()
                    binding.arrTime.text = ""
                    increaseViewSize(binding.constSurfaceView)
                    cameraSource.start(binding.surfaceview.holder)


                    delay(2000)
                    animation.cancelAnimation()
                    binding.animationInvalid.isVisible = false
                }
            }
        }

        binding.btnCheckout.setOnClickListener{
            if(HomeActivity.checkForInternet(this)){
                checkout(binding.vNumber.text.toString().replace("\\s".toRegex(),"").uppercase())
            }
            else{
                networkDialog(this,viewModel)
            }

        }


        binding.vNumber.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val numPlate:Regex =
                    "^[A-Z]{2}\\s?[0-9]{1,2}\\s?[A-Z]{1,2}\\s?[0-9]{4}\$".toRegex()
                if (p0!!.matches(numPlate)){
                    decreaseViewSize(binding.constSurfaceView)
                }
                viewModel.arrivingVehicle(binding.vNumber.text.toString().replace("\\s".toRegex(),"").uppercase())
                viewModel.arrivingVehicleData.observe(this@QrCodeActivity){
                    if (it.response!=null) {
                        binding.arrTime.text = it.response.checkinTime!!.subSequence(11,19)
                        binding.leavingTime.text=it.response.currentTime!!.subSequence(11,19)

                        val date1 = simpleDateFormat.parse(it.response.checkinTime.subSequence(11,19).toString())
                        val date2 = simpleDateFormat.parse(it.response.currentTime.subSequence(11,19).toString())

                        val difference: Long = date2!!.time - date1!!.time
                        val days = (difference / (1000 * 60 * 60 * 24))
                        var hours =
                            ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60))
                        val min =
                            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours)  / (1000*60)
                        hours = if (hours < 0) -hours else hours
                        val time = "$hours:$min"
                        if (min<=59 && hours.toInt()==0){
                            binding.refund.text = it.response.amount + "" +".00 RS"
                        }

                       Log.i("Hours", time)
                    }
                    else{
                        binding.arrTime.text = ""
                        binding.leavingTime.text = ""
                        binding.refund.text = "0.00 RS"
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

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
                            playOnOffSound()
                            binding.vNumber.setText(stringBuilder.toString().replace("\\s".toRegex(),""))
                            binding.capture.isVisible = true
                            cameraSource.stop()
                            decreaseViewSize(binding.constSurfaceView)
                            //binding.constSurfaceView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 280)

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
                cameraSource.start(binding.surfaceview.holder)
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
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
            Config.permissionRequestCode ->{
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
                        cameraSource.start(binding.surfaceview.holder)
                    }
                }
            }
        }
    }


    private fun checkout(result: String) {
        viewModel.checkout(result)

        viewModel.errorMessage.observe(this){
            Log.d("checkout",it.toString())
        }
    }

    override fun onBackPressed() {
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
        finish()
    }


}