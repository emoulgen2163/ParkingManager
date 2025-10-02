package com.mycompany.parkingmanager.presentation.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import com.mycompany.parkingmanager.databinding.FragmentVehicleEntryBinding
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.VehicleType
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.timeFormatFlow
import com.mycompany.parkingmanager.domain.viewModel.VehicleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class VehicleEntryFragment : Fragment() {

    private var _binding: FragmentVehicleEntryBinding? = null
    private val binding get() = _binding!!

    val vehicleViewModel: VehicleViewModel by activityViewModels()
    private lateinit var myView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentVehicleEntryBinding.inflate(inflater, container, false)

        CoroutineScope(Dispatchers.Main).launch{
            val timeFormat = requireContext().timeFormatFlow.first()

            when(timeFormat){
                "12h" -> {
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    binding.textInputEditTextDate.setText(dateFormat)
                }
                "24h" -> {
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    binding.textInputEditTextDate.setText(dateFormat)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, VehicleType.entries)


        binding.vehicleTypeSpinner.adapter = arrayAdapter
        myView = view



        binding.button.setOnClickListener {
            saveVehicle(myView)

            binding.textInputEditText.setText("")
        }
    }

    private fun saveVehicle(view: View){
        val plateNo = binding.textInputEditText.text.toString().uppercase(Locale.ROOT)
        val brand = binding.textInputEditTextBrand.text.toString()
        val vehicleType = binding.vehicleTypeSpinner.selectedItem as VehicleType
        val tariffName = binding.textInputEditTextTariffName.text.toString()

        val requiredAreas = plateNo.isNotEmpty() && brand.isNotEmpty() && tariffName.isNotEmpty()
        val pattern = Regex("^\\d{2}[A-Za-z]+\\d{3}$")
        val plateCondition = plateNo.length == 8 && pattern.matches(plateNo)

        if (requiredAreas && plateCondition){
            val vehicle = Vehicle(plate = plateNo, vehicleType = vehicleType, brand = brand, tariffName = tariffName)
            vehicleViewModel.addVehicle(vehicle)
            Snackbar.make(myView, "Saved Successfully", Snackbar.LENGTH_SHORT).show()
        } else{
            if(!requiredAreas){
                Toast.makeText(myView.context, "Please fill the required areas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(myView.context, "The plate number must match with the standard rules", Toast.LENGTH_SHORT).show()
            }
        }
    }


}