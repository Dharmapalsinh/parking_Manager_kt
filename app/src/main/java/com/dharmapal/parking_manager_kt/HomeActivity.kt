package com.dharmapal.parking_manager_kt

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.Retrofit.RetrofitClientCopy
import com.dharmapal.parking_manager_kt.Utills.CheckNetworkConnection
import com.dharmapal.parking_manager_kt.Utills.Config
import com.dharmapal.parking_manager_kt.Utills.ManagePermissions
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModelFactory
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: MainViewModel
    // creating constant keys for shared preferences.
    private val sharedPref: String = "shared_prefs"
    private var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var delay = 2000
    private lateinit var managePermissions: ManagePermissions
    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onStart() {
        super.onStart()

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

//        when {
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                        Config.Permission_BT_Connect
//                    )
//                }
//            }
//
//        }
//
//        when {
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED -> {
//
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.CAMERA,
//                        //todo:removed storage & BT permissions
//                    ),
//                    Config.permissionRequestCode
//                )
//
//            }
//
//
//        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory= MainViewModelFactory(Repo(RetrofitClientCopy()))
        viewModel= ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]

        viewModel.testqr(Repo2(RetrofitClientCopy()))
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

        submit()

    }

    private fun submit(){
//        val showMe = ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT)
//        showMe.setMessage("Please wait")
//        showMe.setCancelable(true)
//        showMe.setCanceledOnTouchOutside(false)
//        showMe.show()
        val animation=binding.animation
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


                lifecycleScope.launch {

                    delay(5000)
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



//                showMe.dismiss()
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
                    //todo below line
                    viewModel.submit()
                }
                else{

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