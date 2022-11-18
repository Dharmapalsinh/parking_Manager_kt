package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.codemybrainsout.ratingdialog.RatingDialog
import com.dharmapal.parking_manager_kt.BottomFragment.BottomSheetChangePasswordFragment
import com.dharmapal.parking_manager_kt.BottomFragment.BottomSheetLanguageFragment
import com.dharmapal.parking_manager_kt.BottomFragment.BottomSheetSupportFragment
import com.dharmapal.parking_manager_kt.databinding.ActivitySettingBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class  SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val sharedPref: String = "shared_prefs"

    // variable for shared preferences.
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(sharedPref, Context.MODE_PRIVATE)

        binding.ibBackSetting.setOnClickListener {
            finish()
        }

        binding.cvPrinterSetting.setOnClickListener {
            val intent = Intent(this, PrinterListActivity::class.java)
            startActivity(intent)
        }

        binding.cvLanguage.setOnClickListener {
            BottomSheetLanguageFragment().apply {
                show(supportFragmentManager, "BottomSheetLanguageFragment")
            }
        }

        binding.cvChangePassword.setOnClickListener {
            BottomSheetChangePasswordFragment().apply {
                show(supportFragmentManager, "BottomSheetChangePasswordFragment")
            }
        }

        binding.cvSupport.setOnClickListener {
            BottomSheetSupportFragment().apply {
                show(supportFragmentManager, "BottomSheetSupportFragment")
            }
        }

        binding.cvLegal.setOnClickListener {
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        }

        binding.cvWebsite.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://spotiz.com/"))
            startActivity(browserIntent)
        }

        binding.cvLogout.setOnClickListener {
            customExitDialog()
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

//    override fun onBackPressed() {
//        val i = Intent(this, HomeActivity::class.java)
//        startActivity(i)
//        finish()
//    }
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}