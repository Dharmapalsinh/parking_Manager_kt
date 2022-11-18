package com.dharmapal.parking_manager_kt.BottomFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dharmapal.parking_manager_kt.R
import com.dharmapal.parking_manager_kt.databinding.BottomSheetSupportBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetSupportFragment() : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetSupportBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding= BottomSheetSupportBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.ivClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}