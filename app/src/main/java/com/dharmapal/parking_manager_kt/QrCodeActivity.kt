package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.databinding.ActivityQrCodeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewmodel: MainViewmodel
    private var vnumber: TextView? = null
    private  var missing:TextView? = null
    private var pass: EditText? = null
    private var checkout: CardView? = null
    val TAG = "STag"
    var vno: String? = null
    var passno: String? = null
    var flag: String? = "2"
    var lay: RelativeLayout? = null
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
//    private lateinit var code:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        detector=BarcodeDetector.Builder(this).build()
        cameraSource=CameraSource.Builder(this,detector)
            .setAutoFocusEnabled(true)
            .build()
        binding.surfaceView.holder.addCallback(surfacecallback)
        detector.setProcessor(processor)

        lay = findViewById(R.id.relVhcle)

        vnumber = findViewById(R.id.vnumber)
        missing = findViewById(R.id.missingpass1)
        pass = findViewById(R.id.passnoo)
        checkout = findViewById(R.id.checkout)


       binding.checkout.setOnClickListener(View.OnClickListener {

                Checkout(binding.passnoo.text.toString())

        })

        binding.missingpass1.setOnClickListener(View.OnClickListener {
            val i = Intent(this, CheckOutActivity::class.java)
            startActivity(i)


        })
    }


    private val surfacecallback= object :SurfaceHolder.Callback{
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(p0: SurfaceHolder) {
            try {
                cameraSource.start(p0)
            }
            catch (e:Exception){

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
                val qrcodes:SparseArray<Barcode> =p0.detectedItems
                val codes=qrcodes.valueAt(0)
                Log.d("qrcode",codes.displayValue.toString())
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        Scan(codes.displayValue)
                        binding.passnoo.isVisible=true
                        binding.relVhcle.isVisible=false
                    }
                }
            }
            else{

            }
        }
    }
    fun Scan(result: String) {
//        val showMe = ProgressDialog(this@QrCodeActivity, AlertDialog.THEME_HOLO_LIGHT)
//        showMe.setMessage("Please wait")
//        showMe.setCancelable(true)
//        showMe.setCanceledOnTouchOutside(false)
//        showMe.show()

        Log.d("tagged",result)
        viewmodel.scan(result)
//        showMe.dismiss()
        viewmodel.scanData.observe(this){
            Log.d("scan",it.msg.toString())
        }
        viewmodel.errorMessage.observe(this){
            Log.d("scan",it.toString())
        }
    }

    fun Checkout(result: String) {
        val showMe = ProgressDialog(this@QrCodeActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",result)
        viewmodel.checkout(result)
        showMe.dismiss()
        viewmodel.checkoutData.observe(this){
            Log.d("checkout",it.msg.toString())
            Toast.makeText(applicationContext,it.msg,Toast.LENGTH_SHORT).show()
        }
        viewmodel.errorMessage.observe(this){
            Log.d("checkout",it.toString())
        }
 }

    private fun NetworkDialogs(id: String) {
        val dialogs = Dialog(this@QrCodeActivity)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done = dialogs.findViewById<View>(R.id.done) as Button
        done.setOnClickListener {
            dialogs.dismiss()
            Scan(id)
        }
        dialogs.show()
    }
}