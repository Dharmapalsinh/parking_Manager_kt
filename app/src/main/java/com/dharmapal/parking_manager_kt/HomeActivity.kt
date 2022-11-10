package com.dharmapal.parking_manager_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Utills.CheckNetworkConnection
import com.dharmapal.parking_manager_kt.Utills.ManagePermissions
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: MainViewModel
    // creating constant keys for shared preferences.
    private val sharedPref: String = "shared_prefs"
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var delay = 2000
    private lateinit var managePermissions: ManagePermissions
    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onStart() {
        super.onStart()

        submit()

        val list =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
                listOf(
                    Manifest.permission.CAMERA
                )
        }

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this,list,123)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            managePermissions.checkPermissions()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        viewModel.testQr(Repo2(RetrofitClientCopy()))
//         initializing our shared preferences.
        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        callNetworkConnection(application!!,this,this,viewModel)

        binding.btnScanEntry.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

        binding.btnScanexit.setOnClickListener {
            val i = Intent(this, QrCodeActivity::class.java)
            startActivity(i)
        }

        binding.cardView.setOnClickListener {
            val i = Intent(this, SettingActivity::class.java)
            startActivity(i)
        }



    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(applicationContext, "Please Allow All Required Permissions.", Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                                    "package:$packageName"
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun submit(){

        val animation=binding.animation
        binding.animation.isVisible = true
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            animation.playAnimation()
        }.also { runnable = it }, delay.toLong())

        viewModel.submit()
        viewModel.dashboardData.observe(this){
            if(it==null ){
                networkDialog(this,viewModel)
            }
            else {
                Log.d("dashboard", it.toString())

                val ints = ("" + it.occPer).toFloat().roundToInt()

                    binding.availabel.text = it.available.toString()
                    binding.prepaidusers.text = it.prepaidUser.toString()
                    binding.vip.text = it.vipUser.toString()
                    binding.parkingEntry.text = it.totalVehicle.toString()
                    binding.collection.text = it.totalCol
                    binding.occupied.text = it.occupied.toString()
                    binding.circularProgressBar.progress = ints
                    binding.progress.text = it.occPer.toString() + "%"

                    animation.cancelAnimation()
                    handler.removeCallbacks(runnable!!)
                    binding.animation.isVisible=false

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
             val handler = Handler(Looper.getMainLooper())
             var runnable: Runnable? = null
             val delay = 2000
            val dialogs = Dialog(context)
            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogs.setContentView(R.layout.networkdialog)
            dialogs.setCanceledOnTouchOutside(false)
            val done = dialogs.findViewById<View>(R.id.done) as Button
            val animationInternet = dialogs.findViewById<View>(R.id.animation_internet) as LottieAnimationView

             handler.postDelayed(Runnable {
                 handler.postDelayed(runnable!!, delay.toLong())
                 animationInternet.playAnimation()
             }.also { runnable = it }, delay.toLong())

            done.setOnClickListener {

                if (checkForInternet(context)) {
                    dialogs.dismiss()
                    animationInternet.cancelAnimation()
                    handler.removeCallbacks(runnable!!)
                    animationInternet.isVisible = false
                    //todo below line
                    viewModel.submit()
                }

            }
            dialogs.show()
        }

        fun callNetworkConnection(application:Application,lifecycleOwner: LifecycleOwner,context: Context,viewModel: MainViewModel) {
            val checkNetworkConnection = CheckNetworkConnection(application)
            checkNetworkConnection.observe(lifecycleOwner) { isConnected ->
                if (!isConnected) {
                    networkDialog(context,viewModel)
                }
            }

        }
    }
}