package com.mycompany.parkingmanager.presentation.ui.fragments

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.databinding.FragmentSettingsBinding
import com.mycompany.parkingmanager.presentation.ui.activities.MainActivity
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.currencyFlow
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.timeFormatFlow
import com.mycompany.parkingmanager.domain.utils.SpinnerLists
import com.mycompany.parkingmanager.domain.viewModel.TariffViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    val viewModel: TariffViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val dropdownAdapter = ArrayAdapter(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            SpinnerLists.CURRENCY_LIST)
        binding.currencyDropdown.adapter = dropdownAdapter

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Change the color dynamically
        val upArrow = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_back_24)
        upArrow?.colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_ATOP)
        (activity as MainActivity).supportActionBar?.setHomeAsUpIndicator(upArrow)

        binding.toolbar.setNavigationOnClickListener {
            (activity as MainActivity).onBackPressedDispatcher.onBackPressed() // or your custom action
        }

        lifecycleScope.launch {
            val currency = requireContext().currencyFlow.first()
            val index = SpinnerLists.CURRENCY_LIST.indexOf(currency)
            binding.currencyDropdown.setSelection(index)
        }

        lifecycleScope.launch {
            val timeFormat = requireContext().timeFormatFlow.first()
            if (timeFormat == "12h"){
                binding.format12h.isChecked = true
                binding.format24h.isChecked = false
            } else{
                binding.format12h.isChecked = false
                binding.format24h.isChecked = true
            }
        }

        binding.currencyDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = SpinnerLists.CURRENCY_LIST[position]
                PreferenceKeys.saveCurrency(requireContext(), selected)

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        binding.timeFormatGroup.setOnCheckedChangeListener { _, checkedId ->
            val selected = if (checkedId == binding.format12h.id) "12h" else "24h"
            PreferenceKeys.saveTimeFormat(requireContext(), selected)
        }

        return binding.root
    }

}