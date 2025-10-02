package com.mycompany.parkingmanager.presentation.ui.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.presentation.adapters.TariffAdapter
import com.mycompany.parkingmanager.databinding.FragmentAdminBinding
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.currencyFlow
import com.mycompany.parkingmanager.domain.viewModel.TariffViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.navigation.findNavController
import com.mycompany.parkingmanager.domain.utils.CurrencyConverter
import com.mycompany.parkingmanager.domain.utils.SpinnerLists
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first


@AndroidEntryPoint
class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var tariffAdapter: TariffAdapter

    val tariffViewModel: TariffViewModel by activityViewModels()

    var currencySymbol = "$"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdminBinding.inflate(inflater, container, false)

        binding.addTariffButton.setOnClickListener {
            addTariffDialog()
        }

        binding.settingsButton.setOnClickListener {
            it.findNavController().navigate(R.id.settingsFragment)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        lifecycleScope.launch {
            val currency = requireContext().currencyFlow.first()
            when(currency){
                "USD" -> currencySymbol = "$"
                "EUR" -> currencySymbol = "€"
                "GBP" -> currencySymbol = "£"
            }
            tariffAdapter = TariffAdapter {
                showEditTaskDialog(it)
            }

            tariffViewModel.getTariffs().observe(viewLifecycleOwner) { list ->
                val convertedList = currencyConverter(list as MutableList)
                tariffAdapter.differ.submitList(convertedList)
            }

            binding.tariffRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = tariffAdapter
            }
        }

    }

    private fun currencyConverter(list: MutableList<Tariff>): MutableList<Tariff> {
        when(currencySymbol){
            "$" -> {
                list.forEach {
                    when(it.currencySymbol){
                        "€" -> {
                            // converts from euro to dollar
                            val newPrice = it.price * CurrencyConverter.EURO_TO_DOLLAR
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }
                        "£" -> {
                            // converts from pound to dollar
                            val newPrice = it.price * CurrencyConverter.POUND_TO_DOLLAR
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }

                    }

                }
            }
            "€" -> {
                list.forEach {
                    when(it.currencySymbol){
                        "$" -> {
                            // converts from dollar to euro
                            val newPrice = it.price * CurrencyConverter.DOLLAR_TO_EURO
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }
                        "£" -> {
                            // converts from pound to euro
                            val newPrice = it.price * CurrencyConverter.POUND_TO_EURO
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }
                    }
                }
            }
            "£" -> {
                list.forEach {
                    when(it.currencySymbol){
                        "€" -> {
                            // converts from euro to pound
                            val newPrice = it.price * CurrencyConverter.EURO_TO_POUND
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }
                        "$" -> {
                            // converts from dollar to pound
                            val newPrice = it.price * CurrencyConverter.DOLLAR_TO_POUND
                            val roundedPrice =  String.format("%.2f", newPrice).toDouble()
                            it.price = roundedPrice
                            it.currencySymbol = currencySymbol
                        }
                    }
                }
            }
        }

        return list
    }

    private fun addTariffDialog(){
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
        }

        val tariffEditText = EditText(requireContext()).apply{
            hint = "Tariff Name: "
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val numberEditText = EditText(requireContext()).apply {
            hint = "Price: "
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val dropdown = Spinner(requireContext())
        val adapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, SpinnerLists.CURRENCY_LIST)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        dropdown.adapter = adapter

        when(currencySymbol){
            "$" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("USD")
                dropdown.setSelection(index)
            }
            "€" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("EUR")
                dropdown.setSelection(index)
            }
            "£" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("GBP")
                dropdown.setSelection(index)
            }
        }

        layout.addView(tariffEditText)
        layout.addView(numberEditText)
        layout.addView(dropdown)

        AlertDialog.Builder(requireContext()).setTitle("Add Tariff").setView(layout).setPositiveButton("Add"){ dialog, _ ->
            val price = numberEditText.text.toString().toDouble()
            val tariffName = tariffEditText.text.toString()
            var currentSelection = dropdown.selectedItem.toString()

            when(currentSelection){
                "USD" -> currentSelection = "$"
                "EUR" -> currentSelection = "€"
                "GBP" -> currentSelection = "£"
            }

            val tariff = Tariff(tariffName = tariffName, price = price, currencySymbol = currentSelection)
            tariffViewModel.addTariff(tariff)

            dialog.dismiss()
        }.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }.show()


    }

    private fun showEditTaskDialog(tariff: Tariff) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
        }

        val tariffNameInput = EditText(requireContext()).apply {
            setText(tariff.tariffName)
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val priceInput = EditText(requireContext()).apply {
            setText(tariff.price.toString())
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val dropdown = Spinner(requireContext())
        val adapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, SpinnerLists.CURRENCY_LIST)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        dropdown.adapter = adapter

        when(currencySymbol){
            "$" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("USD")
                dropdown.setSelection(index)
            }
            "€" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("EUR")
                dropdown.setSelection(index)
            }
            "£" -> {
                val index = SpinnerLists.CURRENCY_LIST.indexOf("GBP")
                dropdown.setSelection(index)
            }
        }
        layout.addView(tariffNameInput)
        layout.addView(priceInput)
        layout.addView(dropdown)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Tariff")

        builder.setView(layout)

        builder.setPositiveButton("Save"){ dialog, _ ->
            val newTariffName = tariffNameInput.text.toString().trim()
            val newTariffPrice = priceInput.text.toString().trim()
            var currentSelection = dropdown.selectedItem.toString()

            when(currentSelection){
                "USD" -> currentSelection = "$"
                "EUR" -> currentSelection = "€"
                "GBP" -> currentSelection = "£"
            }

            if (newTariffName.isNotEmpty() && newTariffPrice.isNotEmpty()){
                val updatedTariff = tariff.copy(tariffName = newTariffName, price = newTariffPrice.toDouble(), currencySymbol = currentSelection)
                tariffViewModel.updateTariff(updatedTariff)
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

}