package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.codemybrainsout.ratingdialog.RatingDialog
import com.dharmapal.parking_manager_kt.databinding.ActivityHomeBinding
import com.dharmapal.parking_manager_kt.databinding.ActivitySettingBinding
import com.dharmapal.parking_manager_kt.viewmodels.MainViewModel

class  SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val sharedPref: String = "shared_prefs"
    var mypopupWindow: PopupWindow = PopupWindow()

    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)

        binding.tvLogout.setOnClickListener {
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
                finish()
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.dismiss() }
            val alertDialog = builder.create()
            alertDialog.show()
        }

        binding.tvRate.setOnClickListener {
            showDialog()
        }

        binding.tvEnglish.setOnClickListener {
            val  inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.custom_popup_menu, null)

            val english=view.findViewById<TextView>(R.id.tv_English)
            val french=view.findViewById<TextView>(R.id.tv_french)

            mypopupWindow =  PopupWindow(view,500, 250, true)
            mypopupWindow.showAsDropDown(binding.tvEnglish)

            english.setOnClickListener {
                Toast.makeText(this,"English",Toast.LENGTH_SHORT).show()
                mypopupWindow.dismiss()
            }

            french.setOnClickListener {
                Toast.makeText(this,"French",Toast.LENGTH_SHORT).show()
                mypopupWindow.dismiss()
            }
        }

    }

    private fun showDialog() {
        val ratingDialog: RatingDialog = RatingDialog.Builder(this)
            .threshold(3)
            .session(1)
            .ratingBarColor(R.color.colorPrimaryDark)
            .onThresholdCleared { _, rating, thresholdCleared -> Log.i(TAG, "onThresholdCleared: $rating $thresholdCleared") }
            .onThresholdFailed { _, rating, thresholdCleared -> Log.i(TAG, "onThresholdFailed: $rating $thresholdCleared") }
            .onRatingChanged { rating, thresholdCleared -> Log.i(TAG, "onRatingChanged: $rating $thresholdCleared") }
            .onRatingBarFormSubmit { feedback -> Log.i(TAG, "onRatingBarFormSubmit: $feedback") }
            .build()

        ratingDialog.show()
    }


    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}