package com.dharmapal.parking_manager_kt

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewmodelFactory
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewmodel: MainViewmodel
    //private val retrofitService = RetrofitService.getInstance()
    // creating constant keys for shared preferences.
    val SHARED_PREFS: String? = "shared_prefs"

    // key for storing email.
    val EMAIL_KEY = "email_key"

    // key for storing password.
    val PASSWORD_KEY = "password_key"

    // variable for shared preferences.
    private lateinit var sharedpreferences: SharedPreferences

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewmodelFactory(Repo(RetrofitClientCopy()))
        viewmodel= ViewModelProvider(this,viewModelFactory)[MainViewmodel::class.java]

        // initializing our shared preferences.
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);


        binding.print.setOnClickListener(View.OnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        })

        binding.checkout.setOnClickListener(View.OnClickListener {
            val i = Intent(this, QrCodeActivity::class.java)
            startActivity(i)

        })

        binding.logout.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to Logout")
            builder.setPositiveButton(
                "Yes"
            ) { dialog, id ->
                //sessionManager.logoutUser()
                val editor = sharedpreferences.edit()
                editor.clear()
                editor.putString("count","0")
                editor.apply()
                val i = Intent(this, LogInActivity::class.java)
                startActivity(i)
                dialog.dismiss()
//                finish()
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, id -> dialog.dismiss() }
            val alertDialog = builder.create()
            alertDialog.show()
        })

           Submit()
    }

    fun Submit(){
        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        viewmodel.submit()
        showMe.dismiss()
        viewmodel.dashboardData.observe(this){
            if(it==null){
                NetworkDialog()
            }
            else {
                Log.d("dashboard", it.toString())

                val ints = ("" + it.occPer).toFloat().roundToInt().toInt()

                binding.availabel.text = it.available.toString()
                binding.prepaidusers.text = it.prepaidUser.toString()
                binding.vippasses.text = it.vipUser.toString()
                binding.missingpasses.text = it.missingPass.toString()
                binding.todayscollection.text = it.totalCol
                binding.occupied.text = it.occupied.toString()
                binding.circularProgressBar.progress = ints
                binding.progress.text = it.occPer.toString() + "%"
            }
        }
        viewmodel.errorMessage.observe(this){
            Log.d("taggederr",it.toString())

        }

    }

    private fun NetworkDialog() {
        val dialogs = Dialog(this)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.networkdialog)
        dialogs.setCanceledOnTouchOutside(false)
        val done = dialogs.findViewById<View>(R.id.done) as Button
        done.setOnClickListener {
            dialogs.dismiss()
            //Submit()
        }
        dialogs.show()
    }
}