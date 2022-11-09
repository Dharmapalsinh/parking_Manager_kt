package com.dharmapal.parking_manager_kt

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.checkForInternet
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.networkDialog
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityLogInBinding
import com.dharmapal.parking_manager_kt.models.LogInResponse
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//todo:progressbar
class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var viewModel: MainViewModel

    // creating constant keys for shared preferences.
    private val sharedPref = "shared_prefs"

    // key for storing email.
    private val emailKey = "email_key"

    // key for storing password.
    private val passwordKey = "password_key"

    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences
    private  var email: String? =null
    private  var password: String? =null
    private var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var delay = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]


        callNetworkConnection(application!!, this, this, viewModel)
        // getting the data which is stored in shared preferences.
        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)

        // in shared prefs inside het string method
        // we are passing key value as EMAIL_KEY and
        // default value is
        // set to null if not present.
        email = sharedPreferences.getString("EMAIL_KEY", null).toString()
        password = sharedPreferences.getString("PASSWORD_KEY", null).toString()

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
                if (s.isEmpty()) {
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
                if (s.isEmpty()) {
                    password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_lock,
                        0,
                        R.drawable.ic_check_mark1,
                        0
                    )
                }
            }
        })

        viewModel.loginData.observe(this@LogInActivity){
            if (it.status=="200") {

                lifecycleScope.launch {
                    val animation=binding.animation
                    binding.animation.isVisible = true
                    handler.postDelayed(Runnable {
                        handler.postDelayed(runnable!!, delay.toLong())
                        animation.playAnimation()
                    }.also { runnable = it }, delay.toLong())

                    delay(3000)

                    val i = Intent(this@LogInActivity, HomeActivity::class.java)
                    i.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(i)
                    val editor = sharedPreferences.edit()

                    editor.putString(emailKey, number.text.toString())
                    editor.putString(passwordKey, password.text.toString())
                    editor.putString("count", "1")
                    editor.apply()

                    animation.cancelAnimation()
                    handler.removeCallbacks(runnable!!)
                    binding.animation.isVisible = false

                }
            }
            else {
                Toast.makeText(applicationContext, "Invalid Username Or Password", Toast.LENGTH_SHORT).show()
                number.error = "Invalid Username Or Password"
                password.error ="Invalid Username Or Password"
            }
        }

        binding.login.setOnClickListener {

            if (number.text.toString() == "") {
                toast(this, "Please Enter Number First!!!")
            } else if (number.text.toString().length < 10) {
                toast(this, "Please Enter Correct Number!!!")
            } else if (password.text.toString() == "") {
                toast(this, "Please Enter Password First!!!")
            } else {
                if (checkForInternet(this@LogInActivity)){

                    login(number.text.toString(), password.text.toString())


                }
                    else{
                        networkDialog(this@LogInActivity,viewModel)
                    }



            }
        }
    }

    override fun onPause() {
        super.onPause()
//        binding.animation.cancelAnimation()
//        handler.removeCallbacks(runnable!!)
//        binding.animation.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStartEmail",email+password+sharedPreferences.getString("count",null))
        if (sharedPreferences.getString("count",null).toString() == "1") {
            val i = Intent(this@LogInActivity, HomeActivity::class.java)
            startActivity(i)
            finish()
        }

    }

    private fun login(number:String, password:String){
        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        Log.d("tagged",password)
        viewModel.logIn(number,password)
        showMe.dismiss()
        viewModel.loginData.observe(this){
            Log.d("tagged",it.toString())
        }
        viewModel.errorMessage.observe(this){
            Log.d("tagged",it.toString())
        }

    }
    private fun toast(activity: Activity?, Message: String?) {
        val toast = Toast.makeText(this, Message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}