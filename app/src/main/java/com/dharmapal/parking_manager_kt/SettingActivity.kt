package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.codemybrainsout.ratingdialog.RatingDialog
import com.dharmapal.parking_manager_kt.databinding.ActivitySettingBinding

class  SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val sharedPref: String = "shared_prefs"
    private var myPopupWindow: PopupWindow = PopupWindow()

    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)

        binding.tvLogout.setOnClickListener {
            customExitDialog()
        }

        binding.tvRate.setOnClickListener {
            showDialog()
        }

        binding.tvEnglish.setOnClickListener {
            val  inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.custom_popup_menu, null)

            val english=view.findViewById<TextView>(R.id.tv_English)
            val french=view.findViewById<TextView>(R.id.tv_french)

            myPopupWindow =  PopupWindow(view,500, 250, true)
            myPopupWindow.showAsDropDown(binding.tvEnglish)

            english.setOnClickListener {
                Toast.makeText(this,"English",Toast.LENGTH_SHORT).show()
                myPopupWindow.dismiss()
            }

            french.setOnClickListener {
                Toast.makeText(this,"French",Toast.LENGTH_SHORT).show()
                myPopupWindow.dismiss()
            }
        }
    }

    private fun customExitDialog() {
        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
        val delay = 1000

        val dialog = Dialog(this@SettingActivity)

        dialog.setContentView(R.layout.custom_logout_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogButtonYes = dialog.findViewById(R.id.textViewYes) as TextView
        val dialogButtonNo = dialog.findViewById(R.id.textViewNo) as TextView
        val animationLogout = dialog.findViewById<View>(R.id.animation_logout) as LottieAnimationView

        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            animationLogout.playAnimation()
        }.also { runnable = it }, delay.toLong())

        dialogButtonNo.setOnClickListener { // dismiss the dialog
            dialog.dismiss()
        }
        dialogButtonYes.setOnClickListener { // dismiss the dialog and exit the exit
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putString("count","0")
            editor.apply()
            val i = Intent(this, LogInActivity::class.java)
            startActivity(i)
            dialog.dismiss()
            animationLogout.cancelAnimation()
            handler.removeCallbacks(runnable!!)
            animationLogout.isVisible = false
            finish()
        }
        dialog.show()
    }


    @SuppressLint("ResourceType")
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