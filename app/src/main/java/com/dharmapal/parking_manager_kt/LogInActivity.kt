package com.dharmapal.parking_manager_kt

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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


//todo:progressbar
class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var viewmodel: MainViewmodel
//    private val retrofitService = RetrofitClientCopy().spotizInstance

    // creating constant keys for shared preferences.
    val SHARED_PREFS = "shared_prefs"

    // key for storing email.
    val EMAIL_KEY = "email_key"

    // key for storing password.
    val PASSWORD_KEY = "password_key"

    // variable for shared preferences.
    private lateinit var sharedpreferences: SharedPreferences
    private  var email: String? =null
    private  var password: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]


        // getting the data which is stored in shared preferences.
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // in shared prefs inside het string method
        // we are passing key value as EMAIL_KEY and
        // default value is
        // set to null if not present.
        email = sharedpreferences.getString("EMAIL_KEY", null).toString();
        password = sharedpreferences.getString("PASSWORD_KEY", null).toString();

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

                val editor = sharedpreferences.edit()

                editor.putString(EMAIL_KEY, number.text.toString())
                editor.putString(PASSWORD_KEY, password.text.toString())
            editor.putString("count","1")
                editor.apply()

                val i = Intent(this, HomeActivity::class.java)
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)

                startActivity(i)
//            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("onstartemail",email+password+sharedpreferences.getString("count",null))
        if (sharedpreferences.getString("count",null).toString() == "1") {
            val i = Intent(this@LogInActivity, HomeActivity::class.java)
            startActivity(i)
            finish()
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