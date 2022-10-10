package com.dharmapal.parking_manager_kt

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityLogInBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory

//todo:progressbar, session Manager
class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var viewmodel: MainViewmodel
//    private val retrofitService = RetrofitClientCopy().spotizInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        val number=binding.number
        val password=binding.pass
        binding.fpass.setOnClickListener {
            val intent=Intent(applicationContext,ForgotPassword_Activity::class.java)
            startActivity(intent)
        }

        binding.number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.number.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_user,
                    0,
                    R.drawable.ic_check_mark,
                    0
                )
            }
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) {
                    binding.number.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_user,
                        0,
                        R.drawable.ic_check_mark1,
                        0
                    )
                }
            }
        })

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                password.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_check_mark,
                    0
                )
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) {
                    password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_lock,
                        0,
                        R.drawable.ic_check_mark1,
                        0
                    )
                }
            }
        })

        binding.login.setOnClickListener {
//            if (number.text.toString() == "") {
//                toast(this, "Please Enter Number First!!!")
//            } else if (number.text.toString().length < 10) {
//                toast(this, "Please Enter Correct Number!!!")
//            } else if (password.text.toString() == "") {
//                toast(this, "Please Enter Password First!!!")
//            } else {
                Login(number.text.toString(), password.text.toString())

                val i = Intent(this, HomeActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)

                startActivity(i)
//            }
        }
    }

    fun Login(number:String, password:String){
        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",password)
        viewmodel.logIn(number,password)
        showMe.dismiss()
        viewmodel.loginData.observe(this){
            Log.d("tagged",it.id.toString())
        }
        viewmodel.errorMessage.observe(this){
            Log.d("taggederr",it.toString())
        }

    }
    fun toast(activity: Activity?, Message: String?) {
        val toast = Toast.makeText(this, Message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}