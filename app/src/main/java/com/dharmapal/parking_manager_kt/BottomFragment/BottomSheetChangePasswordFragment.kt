package com.dharmapal.parking_manager_kt.BottomFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dharmapal.parking_manager_kt.R
import com.dharmapal.parking_manager_kt.databinding.BottomSheetChangePasswordBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetChangePasswordFragment() : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetChangePasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding= BottomSheetChangePasswordBinding.inflate(layoutInflater)


        return binding.root
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.ivClosePassword.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding.btnChangePasswordSubmit.setOnClickListener {
            if (binding.etNewPassword.text.toString() != binding.etConformPassword.text.toString()){

                binding.conformPasswordContainer.helperText = "Confirm password is not correct"

            }

        }

    }
}