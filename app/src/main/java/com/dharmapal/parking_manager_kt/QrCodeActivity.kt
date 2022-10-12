package com.dharmapal.parking_manager_kt

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityQrCodeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewmodel: MainViewmodel
//    private var mCodeScanner: CodeScanner? = null
//    private var scannerView: CodeScannerView? = null
    private var vnumber: TextView? = null
    private  var missing:TextView? = null
    private var pass: EditText? = null
    private var checkout: CardView? = null
    val TAG = "STag"
    var vno: String? = null
    var passno: String? = null
    var flag: String? = "2"
    var lay: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

//        scannerView = findViewById(R.id.scanner_view)
//        mCodeScanner = CodeScanner(this, scannerView)
//        mCodeScanner.setDecodeCallback(object : DecodeCallback() {
//            fun onDecoded(@NonNull result: Result) {
//                runOnUiThread {
//                    passno = result.getText()
//                    Sc an(result.getText())
//                }
//            }
//        })

        lay = findViewById(R.id.relVhcle)

        vnumber = findViewById(R.id.vnumber)
        missing = findViewById(R.id.missingpass1)
        pass = findViewById(R.id.passnoo)
        checkout = findViewById(R.id.checkout)


       binding.checkout.setOnClickListener(View.OnClickListener {

            if (flag == "2") {
                Checkout(binding.passnoo.text.toString())
            } else {
                Checkout(passno!!)
            }
        })

        binding.missingpass1.setOnClickListener(View.OnClickListener {
            val i = Intent(this, CheckOutActivity::class.java)
            startActivity(i)


        })
    }


    fun Scan(result: String) {
        val showMe = ProgressDialog(this@QrCodeActivity, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",result)
        viewmodel.scan(result)
        showMe.dismiss()
        viewmodel.scanData.observe(this){
            Log.d("scan",it.msg.toString())
        }
        viewmodel.errorMessage.observe(this){
            Log.d("scan",it.toString())
        }

//        val url: String = Config.scan
//        mRequestQueue = Volley.newRequestQueue(this@QRCodeActivity)
//        stringRequest = object : StringRequest(Request.Method.POST, url,
//            object : Listener<String?>() {
//                fun onResponse(response: String?) {
//                    showMe.dismiss()
//                    var j: JSONObject? = null
//                    try {
//                        j = JSONObject(response)
//                        val status = j.getString("status")
//                        if (status == "200") {
//                            vno = j.getString("vehicle_no")
//                            vnumber.setText(vno)
//                        } else {
//                            showMe.dismiss()
//                            val toast = Toast.makeText(
//                                this@QRCodeActivity,
//                                "" + j.getString("msg"),
//                                Toast.LENGTH_SHORT
//                            )
//                            toast.setGravity(Gravity.CENTER, 0, 0)
//                            toast.show()
//
//                            /* finish();
//                                overridePendingTransition( 0, 0);
//                                startActivity(getIntent());
//                                overridePendingTransition( 0, 0);*/pass.setVisibility(View.VISIBLE)
//                            lay.setVisibility(View.GONE)
//                        }
//                    } catch (e: JSONException) {
//                        Log.e("TAG", "Something Went Wrong")
//                    }
//                }
//            },
//            object : ErrorListener() {
//                fun onErrorResponse(error: VolleyError?) {
//                    showMe.dismiss()
//                    NetworkDialogs(result)
//                }
//            }) {
//            @get:Throws(AuthFailureError::class)
//            val headers: Map<String, String>?
//                get() {
//                    val headers: MutableMap<String, String> = HashMap()
//                    headers["apikey"] = "d29985af97d29a80e40cd81016d939af"
//                    return headers
//                }
//
//            @get:Throws(AuthFailureError::class)
//            val params: Map<String, String>?
//                get() {
//                    val headers: MutableMap<String, String> = HashMap()
//                    headers["pass_no"] = result
//                    return headers
//                }
//        }
//        stringRequest.setTag(com.wk.tech.carparking.Activities.QRCodeActivity.TAG)
//        mRequestQueue.add(stringRequest)
//        stringRequest.setRetryPolicy(
//            DefaultRetryPolicy(
//                60000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//            )
//        )
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
            Log.d("scan",it.msg.toString())
        }
        viewmodel.errorMessage.observe(this){
            Log.d("scan",it.toString())
        }

//        val url: String = Config.checkouts
//        mRequestQueue = Volley.newRequestQueue(this@QRCodeActivity)
//        stringRequest = object : StringRequest(Request.Method.POST, url,
//            object : Listener<String?>() {
//                fun onResponse(response: String?) {
//                    showMe.dismiss()
//                    var j: JSONObject? = null
//                    try {
//                        j = JSONObject(response)
//                        val status = j.getString("status")
//                        if (status == "200") {
//                            val toast = Toast.makeText(
//                                this@QRCodeActivity,
//                                "" + j.getString("msg"),
//                                Toast.LENGTH_SHORT
//                            )
//                            toast.setGravity(Gravity.CENTER, 0, 0)
//                            toast.show()
//                            finish()
//                            overridePendingTransition(0, 0)
//                            startActivity(intent)
//                            overridePendingTransition(0, 0)
//                        } else {
//                            showMe.dismiss()
//                            val toast = Toast.makeText(
//                                this@QRCodeActivity,
//                                "" + j.getString("msg"),
//                                Toast.LENGTH_SHORT
//                            )
//                            toast.setGravity(Gravity.CENTER, 0, 0)
//                            toast.show()
//                        }
//                    } catch (e: JSONException) {
//                        Log.e("TAG", "Something Went Wrong")
//                    }
//                }
//            },
//            object : ErrorListener() {
//                fun onErrorResponse(error: VolleyError?) {
//                    showMe.dismiss()
//                    NetworkDialogs(result)
//                }
//            }) {
//            @get:Throws(AuthFailureError::class)
//            val headers: Map<String, String>?
//                get() {
//                    val headers: MutableMap<String, String> = HashMap()
//                    headers["apikey"] = "d29985af97d29a80e40cd81016d939af"
//                    return headers
//                }
//
//            @get:Throws(AuthFailureError::class)
//            val params: Map<String, String>?
//                get() {
//                    val headers: MutableMap<String, String> = HashMap()
//                    headers["pass_no"] = result
//                    return headers
//                }
//        }
//        stringRequest.setTag(com.wk.tech.carparking.Activities.QRCodeActivity.TAG)
//        mRequestQueue.add(stringRequest)
//        stringRequest.setRetryPolicy(
//            DefaultRetryPolicy(
//                60000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//            )
//        )
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