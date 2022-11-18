package com.dharmapal.parking_manager_kt

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dharmapal.parking_manager_kt.databinding.ActivityPrivacyBinding
import com.dharmapal.parking_manager_kt.databinding.ActivitySettingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PrivacyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyBinding

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.tvPrivacy.setTextColor(R.color.white)



        lifecycleScope.launch {
            binding.animationPrivacy.isVisible = true
            binding.animationPrivacy.playAnimation()

            binding.webView.webViewClient = WebViewClient()
            binding.webView.loadUrl("https://app.spotiz.com/content/en/privacy-policy")

            delay(1000)
            binding.animationPrivacy.cancelAnimation()
            binding.animationPrivacy.isVisible=false

            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.setSupportZoom(true)
        }




        binding.ibBackPrivacy.setOnClickListener {
            finish()
        }

        binding.tvPrivacy.setOnClickListener {
                binding.tvPrivacy.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tvTerm.setTextColor(R.color.colorPrimaryDark)

            lifecycleScope.launch {
                binding.animationPrivacy.isVisible = true
                binding.animationPrivacy.playAnimation()

                binding.webView.webViewClient = WebViewClient()
                binding.webView.loadUrl("https://app.spotiz.com/content/en/privacy-policy")

                binding.webView.settings.javaScriptEnabled = true
                binding.webView.settings.setSupportZoom(true)
                delay(1000)
                binding.animationPrivacy.cancelAnimation()
                binding.animationPrivacy.isVisible=false
            }

        }
        binding.tvTerm.setOnClickListener {
                binding.tvPrivacy.setTextColor(R.color.colorPrimaryDark)
                binding.tvTerm.setTextColor(Color.parseColor("#FFFFFF"))

            lifecycleScope.launch {
                binding.animationPrivacy.isVisible = true
                binding.animationPrivacy.playAnimation()

                binding.webView.webViewClient = WebViewClient()
                binding.webView.loadUrl("https://app.spotiz.com/content/en/terms-and-conditions")

                binding.webView.settings.javaScriptEnabled = true
                binding.webView.settings.setSupportZoom(true)
                delay(1000)
                binding.animationPrivacy.cancelAnimation()
                binding.animationPrivacy.isVisible = false
            }

        }

    }
}