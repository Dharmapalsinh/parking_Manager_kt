package com.dharmapal.parking_manager_kt


import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityForgotPasswordBinding
import com.dharmapal.parking_manager_kt.models.ForgotPasswordReq
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory

class ForgotPassword_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitClientCopy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(retrofitService))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        val email=binding.email

        binding.send.setOnClickListener {
            val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
            if (!email.text.contains(EMAIL_REGEX.toRegex())) {
                val toast = Toast.makeText(
                    this@ForgotPassword_Activity,
                    "Please Enter Valid Email Address",
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                send()
            }
        }
    }


    private fun send() {
//        val showMe = ProgressDialog(this@ForgetPassword)
//        showMe.setMessage("Please wait")
//        showMe.setCancelable(true)
//        showMe.setCanceledOnTouchOutside(false)
//        showMe.show()
        viewModel.forgotPassword(ForgotPasswordReq(""))
        viewModel.forgotpassData.observe(this){
            Toast.makeText(applicationContext,it,Toast.LENGTH_SHORT).show()
        }
    }


}