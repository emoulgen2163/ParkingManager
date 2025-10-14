package com.mycompany.parkingmanager.presentation.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mycompany.parkingmanager.presentation.adapters.VehicleListAdapter
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.databinding.FragmentVehicleListBinding
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.VehicleType
import com.mycompany.parkingmanager.domain.utils.DateUtils
import com.mycompany.parkingmanager.domain.utils.SpinnerLists
import com.mycompany.parkingmanager.domain.utils.TimeConverter
import com.mycompany.parkingmanager.domain.viewModel.HistoryViewModel
import com.mycompany.parkingmanager.domain.viewModel.TariffViewModel
import com.mycompany.parkingmanager.domain.viewModel.VehicleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class VehicleListFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentVehicleListBinding? = null
    private val binding get() = _binding!!

    val vehicleViewModel: VehicleViewModel by activityViewModels()

    val tariffViewModel by activityViewModels<TariffViewModel>()

    val historyViewModel by activityViewModels<HistoryViewModel>()
    private lateinit var vehicleAdapter: VehicleListAdapter

    var itemExists = false

    val createFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            uri?.let {
                vehicleViewModel.exportVehicles(it)
            }
    }

    val openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            vehicleViewModel.importVehicles(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVehicleListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpRecyclerView()

        binding.searchView.isSubmitButtonEnabled = false
        binding.searchView.setOnQueryTextListener(this)

        val dropdownAdapter = ArrayAdapter(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            SpinnerLists.FILTER_LIST)
        binding.spinner.adapter = dropdownAdapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        lifecycleScope.launch {
                            vehicleViewModel.getVehiclesByTariffName().collectLatest {
                                vehicleAdapter.submitData(it)
                            }
                        }
                    }
                    1 -> {
                        lifecycleScope.launch {
                            vehicleViewModel.getVehiclesByBrand().collectLatest {
                                vehicleAdapter.submitData(it)
                            }
                        }
                    }

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }

        VehicleType.entries.forEach { vehicleType ->
            val chip = Chip(requireContext()).apply {
                text = vehicleType.name
                isCheckable = true
            }

            binding.chipGroup.addView(chip)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            for (id in checkedIds){
                val chip: Chip? = group.findViewById(id)
                val selected: VehicleType? = VehicleType.entries.find {
                    it.name == chip?.text
                }
                vehicleViewModel.setFilter(selected)
            }
        }

        binding.exportButton.setOnClickListener {
            exportVehicles()
            Toast.makeText(requireContext(), "Exported successfully", Toast.LENGTH_SHORT).show()
        }

        binding.importButton.setOnClickListener {
            importVehicles()
        }


    }

    @SuppressLint("CheckResult")
    private fun setUpRecyclerView() {

        vehicleAdapter = VehicleListAdapter { vehicle ->

            val exitTime = System.currentTimeMillis()

            tariffViewModel.getTariff(vehicle.tariffName).observe(viewLifecycleOwner){ tariff ->

                tariff?.let {
                    val duration = TimeConverter().millisToMinute(exitTime - vehicle.entryTime)

                    val fee = tariff.price * duration

                    val entryDate = LocalDate.now()

                    val exitDate = Instant.ofEpochMilli(exitTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val history = History(
                        plate = vehicle.plate,
                        vehicleType = vehicle.vehicleType,
                        entryTime = vehicle.entryTime,
                        exitTime = exitTime,
                        entryDate = entryDate,
                        exitDate = exitDate,
                        tariffName = it.tariffName,
                        fee = fee,
                    )

                    historyViewModel.insert(history)
                    vehicleViewModel.deleteVehicle(vehicle)
                }?: run {
                    Toast.makeText(requireContext(), "Tariff not found!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = vehicleAdapter
        }
        vehicleAdapter.addLoadStateListener { loadState ->
            itemExists = vehicleAdapter.snapshot().items.filterNotNull().any {
                it.plate.trim().isNotEmpty()
            }
            updateUI()
        }
        hideProgressBar()
        setUpSwipeRefresh()
        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            vehicleViewModel.vehicle.collectLatest { vehicle ->
                vehicleAdapter.submitData(vehicle)
            }
        }
    }

    fun setUpSwipeRefresh(){
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.noItemsAdded.visibility = View.INVISIBLE
            fetchData()
        }
    }

    private fun updateUI() {
        if (itemExists) {
            binding.noItemsAdded.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        } else {
            binding.noItemsAdded.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
        }
    }

    private fun showProgressBar(){
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        lifecycleScope.launch {
            delay(3000)
            binding.progressBar.visibility = View.GONE
        }
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchPlate(newText)
        }
        return true
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        // searchPlate(query)
        return false
    }

    private fun searchPlate(query: String?) {
        val searchQuery = "%$query"

        lifecycleScope.launch {
            vehicleViewModel.searchByPlate(searchQuery).collectLatest {
                vehicleAdapter.submitData(it)
            }
        }
    }


    fun exportVehicles() {
        createFileLauncher.launch("vehicles_backup.json")
    }

    fun importVehicles() {
        openFileLauncher.launch(arrayOf("application/json"))
    }


}