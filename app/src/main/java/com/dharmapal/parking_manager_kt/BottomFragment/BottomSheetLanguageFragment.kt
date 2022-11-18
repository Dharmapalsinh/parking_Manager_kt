package com.dharmapal.parking_manager_kt.BottomFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.dharmapal.parking_manager_kt.R
import com.dharmapal.parking_manager_kt.databinding.BottomSheetLanguageBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetLanguageFragment: BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLanguageBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding= BottomSheetLanguageBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val languages = resources.getStringArray(R.array.Languages)

        // access the spinner
        val spinner = binding.spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                /*Toast.makeText(this@MainActivity,
                    getString(R.string.selected_item) + " " +
                            "" + languages[position], Toast.LENGTH_SHORT).show()*/
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }


        binding.ivClose1.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}
