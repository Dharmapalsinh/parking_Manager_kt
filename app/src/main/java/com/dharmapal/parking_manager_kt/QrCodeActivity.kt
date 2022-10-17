package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.networkDialog
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityQrCodeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewModel: MainViewModel
    private var vNumber: TextView? = null
    private  var missing:TextView? = null
    private var pass: EditText? = null
    private var checkout: CardView? = null
    private var lay: RelativeLayout? = null
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        detector=BarcodeDetector.Builder(this).build()
        cameraSource=CameraSource.Builder(this,detector)
            .setAutoFocusEnabled(true)
            .build()
        binding.surfaceview.holder.addCallback(surfaceCallback)
        detector.setProcessor(processor)

        lay = findViewById(R.id.relVhcle)

        vNumber = findViewById(R.id.vnumber)
//        missing = findViewById(R.id.missingpass1)
//        pass = findViewById(R.id.passnoo)
        checkout = findViewById(R.id.btn_checkout)

        callNetworkConnection(application!!, this, this, viewModel)

       binding.btnCheckout.setOnClickListener{
           if(HomeActivity.checkForInternet(this)){
               checkout(binding.passNum.text.toString())
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
    }

    private val surfaceCallback= object :SurfaceHolder.Callback{
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(p0: SurfaceHolder) {
            try {
                cameraSource.start(p0)
            }
            catch (_:Exception){

            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }
    }

    private val processor=object :Detector.Processor<Barcode>{
        override fun release() {

        }

        override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
            if (p0!=null && p0.detectedItems.isNotEmpty()){
                val qrCodes:SparseArray<Barcode> =p0.detectedItems
                val codes=qrCodes.valueAt(0)
                Log.d("qrcode",codes.displayValue.toString())
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        scan(codes.displayValue)
//                        binding.passnoo.isVisible=true
//                        binding.relVhcle.isVisible=false
                    }
                }
            }
            else{

            }
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


