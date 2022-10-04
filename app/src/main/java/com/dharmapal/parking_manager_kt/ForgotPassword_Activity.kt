package com.dharmapal.parking_manager_kt

//import com.dharmapal.parking_manager_kt.Retrofit.RetrofitService
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityForgotPasswordBinding
import com.dharmapal.parking_manager_kt.models.ForgotPassword_Req
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory
import org.json.JSONException
import org.json.JSONObject
import javax.xml.transform.ErrorListener

class ForgotPassword_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewmodel: MainViewmodel
    private val retrofitService = RetrofitClientCopy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(retrofitService))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        val email=binding.email

        binding.send.setOnClickListener {
            if (email.getText().toString() == "") {
                val toast = Toast.makeText(
                    this@ForgotPassword_Activity,
                    "Please Enter Email Address",
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                Send()
            }
        }
    }


    fun Send() {
//        val showMe = ProgressDialog(this@ForgetPassword)
//        showMe.setMessage("Please wait")
//        showMe.setCancelable(true)
//        showMe.setCanceledOnTouchOutside(false)
//        showMe.show()
        viewmodel.forgotPassword(ForgotPassword_Req(""))
    }

    private fun NetworkDialog() {
        val dialogs = Dialog(this@ForgotPassword_Activity)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done = dialogs.findViewById<View>(R.id.done) as Button
        done.setOnClickListener {
            dialogs.dismiss()
            Send()
        }
        dialogs.show()
    }
}