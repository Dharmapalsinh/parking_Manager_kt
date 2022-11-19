package com.dharmapal.parking_manager_kt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.callNetworkConnection
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.checkForInternet
import com.dharmapal.parking_manager_kt.HomeActivity.Companion.networkDialog
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityLogInBinding
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
    private var runnable: Runnable? = null
    private var delay = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        callNetworkConnection(application!!, this, this, viewModel)
        // getting the data which is stored in shared preferences.
        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)

        email = sharedPreferences.getString("EMAIL_KEY", null).toString()
        password = sharedPreferences.getString("PASSWORD_KEY", null).toString()

        val email=binding.emailMobile
        val password=binding.pass
        binding.fpass.setOnClickListener {
            val intent=Intent(applicationContext,ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        /*binding.number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.number.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_user,
                    0,
                    0,
                    0
                )
            }
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    binding.number.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_user,
                        0,
                        0,
                        0
                    )
                }
            }
        })*/

       /* binding.pass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                password.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    0,
                    0
                )
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    password.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_lock,
                        0,
                       0,
                        0
                    )
                }
            }
        })*/

        viewModel.loginData.observe(this@LogInActivity){
            if (it.status=="200") {

               /* lifecycleScope.launch {
                    val animation=binding.animationLogin
                    binding.animationLogin.isVisible = true

                    handler.postDelayed(Runnable {
                        handler.postDelayed(runnable!!, delay.toLong())
                        animation.playAnimation()
                    }.also { it1 -> runnable = it1 }, delay.toLong())

                    delay(3500)*/

                    val i = Intent(this@LogInActivity, HomeActivity::class.java)
                    startActivity(i)
                    val editor = sharedPreferences.edit()

                    editor.putString(emailKey, email.text.toString())
                    editor.putString(passwordKey, password.text.toString())
                    editor.putString("count", "1")
                    editor.apply()

                   /* animation.cancelAnimation()
                    handler.removeCallbacks(runnable!!)
                    binding.animationLogin.isVisible = false

                }*/
            }
            else {
                //Toast.makeText(applicationContext, "Invalid Username Or Password", Toast.LENGTH_SHORT).show()

                binding.passwordContainer.helperText = "Invalid Username Or Password"
            }
        }

        binding.login.setSafeOnClickListener {

            if (email.text.toString() == "") {
                toast("Please Enter Number First!!!")
            } else if (email.text.toString().length < 10) {
                toast("Please Enter Correct Number!!!")
            } else if (password.text.toString() == "") {
                toast("Please Enter Password First!!!")
            } else {
                if (checkForInternet(this@LogInActivity)){

                    login(email.text.toString(), password.text.toString())
                }
                    else{
                        networkDialog(this@LogInActivity,viewModel)
                    }
            }
        }
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

        Log.d("tagged",password)
        viewModel.logIn(number,password)
        viewModel.loginData.observe(this){
            Log.d("tagged",it.toString())
        }
        viewModel.errorMessage.observe(this){
            Log.d("tagged",it.toString())
        }
    }
    private fun toast(Message: String?) {
        val toast = Toast.makeText(this, Message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }
}