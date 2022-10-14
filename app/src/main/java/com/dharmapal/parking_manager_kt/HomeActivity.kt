package com.dharmapal.parking_manager_kt

import com.dharmapal.parking_manager_kt.Utills.CheckNetworkConnection
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: MainViewModel
    //private val retrofitService = RetrofitService.getInstance()
    // creating constant keys for shared preferences.
    private val sharedPref: String = "shared_prefs"

    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        // initializing our shared preferences.
        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        callNetworkConnection(application!!,this,this,viewModel)

        binding.print.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        binding.checkout.setOnClickListener {
            val i = Intent(this, QrCodeActivity::class.java)
            startActivity(i)
        }

        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to Logout")
            builder.setPositiveButton(
                "Yes"
            ) { dialog, _ ->
                //sessionManager.logoutUser()
                val editor = sharedPreferences.edit()
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
            ) { dialog, _ -> dialog.dismiss() }
            val alertDialog = builder.create()
            alertDialog.show()
        }

        submit()

    }

    private fun submit(){
        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
        showMe.setMessage("Please wait")
        showMe.setCancelable(true)
        showMe.setCanceledOnTouchOutside(false)
        showMe.show()

        viewModel.submit()
        showMe.dismiss()
        viewModel.dashboardData.observe(this){
            if(it==null ){
                networkDialog(this,viewModel)
            }
            else {
                Log.d("dashboard", it.toString())

                val ints = ("" + it.occPer).toFloat().roundToInt()

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
        viewModel.errorMessage.observe(this){
            Log.d("error",it.toString())

        }


    }




    companion object{

         fun checkForInternet(context: Context): Boolean {

            // register activity with the connectivity manager service
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // if the android version is equal to M
            // or greater we need to use the
            // NetworkCapabilities to check what type of
            // network has the internet connection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Returns a Network object corresponding to
                // the currently active default data network.
                val network = connectivityManager.activeNetwork ?: return false

                // Representation of the capabilities of an active network.
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

                return when {
                    // Indicates this network uses a Wi-Fi transport,
                    // or WiFi has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                    // Indicates this network uses a Cellular transport. or
                    // Cellular has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                    // else return false
                    else -> false
                }
            } else {
                // if the android version is below M
                @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return networkInfo.isConnected
            }
        }

         fun networkDialog(context: Context,viewModel: MainViewModel) {
            val dialogs = Dialog(context)
            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogs.setContentView(R.layout.networkdialog)
            dialogs.setCanceledOnTouchOutside(false)
            val done = dialogs.findViewById<View>(R.id.done) as Button
            done.setOnClickListener {
                //Submit()
                if (checkForInternet(context)){
                    dialogs.dismiss()
                    viewModel.submit()
                }
            }
            dialogs.show()
        }

        fun callNetworkConnection(application:Application,lifecycleOwner: LifecycleOwner,context: Context,viewModel: MainViewModel) {
            val checkNetworkConnection = CheckNetworkConnection(application)
            checkNetworkConnection.observe(lifecycleOwner) { isConnected ->
                if (isConnected) {

                } else {
                    networkDialog(context,viewModel)
                }
            }

        }
    }
}