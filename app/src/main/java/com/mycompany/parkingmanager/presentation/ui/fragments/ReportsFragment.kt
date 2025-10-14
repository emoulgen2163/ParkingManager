package com.mycompany.parkingmanager.presentation.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.mycompany.parkingmanager.databinding.FragmentReportsBinding
import com.mycompany.parkingmanager.domain.utils.SpinnerLists
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.itextpdf.kernel.colors.ColorConstants
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.domain.SummaryModel
import com.mycompany.parkingmanager.domain.utils.ChartDataLabels
import com.mycompany.parkingmanager.domain.utils.DateUtils
import com.mycompany.parkingmanager.domain.utils.PDFHelper
import com.mycompany.parkingmanager.domain.viewModel.HistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel by activityViewModels<HistoryViewModel>()
    var index = 0

    companion object {
        val colorPalette = listOf(
            "#3F51B5", // Blue
            "#FF5722", // Orange
            "#4CAF50", // Green
            "#FFC107", // Yellow
            "#9C27B0", // Purple
            "#00BCD4", // Cyan
            "#E91E63"  // Pink
        )
    }

    val graphMap = linkedMapOf<String, Any>()
    var isPdfGenerateOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =  FragmentReportsBinding.inflate(inflater, container, false)

        binding.dropdownDate.adapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, SpinnerLists.DATE_LIST)
        binding.dropdownCategory.adapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, SpinnerLists.CATEGORY)

        binding.dropdownDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                val selectedDate = binding.dropdownDate.selectedItem as? String ?: return
                val selectedCategory = binding.dropdownCategory.selectedItem as? String ?: return

                updateCharts(selectedCategory, selectedDate)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.dropdownCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                selectedView: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = binding.dropdownCategory.selectedItem as? String ?: return
                val selectedDate = binding.dropdownDate.selectedItem as? String ?: return

                updateCharts(selectedCategory, selectedDate)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }


        binding.share.setOnClickListener {
            // val pieChartBitmap = BitmapHelper.getChartBitmap(binding.relativeLayout)
            historyViewModel.getIncomeByPlateNumber().observe(viewLifecycleOwner){ summaryModels ->
                lifecycleScope.launch {
                    PDFHelper.generateReport(requireContext(), summaryModels, graphMap) { file ->
                        PDFHelper.sharePDF(requireContext(), file)
                    }
                }
            }
        }

        binding.download.setOnClickListener {
            setUpChartData()
        }

        return binding.root
    }

    private fun updateCharts(category: String, date: String) {
        when (category) {
            "Tariff Name" -> when (date) {
                "Today" -> observeVehiclesByTariff(DateUtils.startOfToday(), DateUtils.endOfToday(), "Today") {}
                "This Week" -> observeVehiclesByTariff(DateUtils.startOfThisWeek(), DateUtils.endOfThisWeek(), "This Week") {}
                "Last Week" -> observeVehiclesByTariff(DateUtils.startOfLastWeek(), DateUtils.endOfLastWeek(), "Last Week") {}
                "This Month" -> observeVehiclesByTariff(DateUtils.startOfThisMonth(), DateUtils.endOfThisMonth(), "This Month") {}
                "This Year" -> observeVehiclesByTariff(DateUtils.startOfThisYear(), DateUtils.endOfThisYear(), "This Year") {}
            }

            "Plate Number" -> when (date) {
                "Today" -> observeVehiclesByPlate(DateUtils.startOfToday(), DateUtils.endOfToday(), "Today") {}
                "This Week" -> observeVehiclesByPlate(DateUtils.startOfThisWeek(), DateUtils.endOfThisWeek(), "This Week") {}
                "Last Week" -> observeVehiclesByPlate(DateUtils.startOfLastWeek(), DateUtils.endOfLastWeek(), "Last Week") {}
                "This Month" -> observeVehiclesByPlate(DateUtils.startOfThisMonth(), DateUtils.endOfThisMonth(), "This Month") {}
                "This Year" -> observeVehiclesByPlate(DateUtils.startOfThisYear(), DateUtils.endOfThisYear(), "This Year") {}
            }
        }
    }


    private fun setUpChartData() {
        isPdfGenerateOn = true

        // List of time ranges and their corresponding date pairs
        val timeRanges = listOf(
            SpinnerLists.DATE_LIST[0] to (DateUtils.startOfToday() to DateUtils.endOfToday()),
            SpinnerLists.DATE_LIST[1] to (DateUtils.startOfThisWeek() to DateUtils.endOfThisWeek()),
            SpinnerLists.DATE_LIST[2] to (DateUtils.startOfLastWeek() to DateUtils.endOfLastWeek()),
            SpinnerLists.DATE_LIST[3] to (DateUtils.startOfThisMonth() to DateUtils.endOfThisMonth()),
            SpinnerLists.DATE_LIST[4] to (DateUtils.startOfThisYear() to DateUtils.endOfThisYear())
        )

        // total number of steps = (number of time ranges * 2)
        val totalSteps = timeRanges.size * 2

        // Safety guard — reset index if it somehow goes out of range
        if (index >= totalSteps) {
            index = 0
            isPdfGenerateOn = false
            openAlertDialog()
            return
        }

        // Determine which time range and chart type to use
        val (label, range) = timeRanges[index / 2]
        val (start, end) = range

        val isTariff = index % 2 == 0

        Log.d("index", "index: $index")

        if (isTariff) {
            observeVehiclesByTariff(start, end, label) {
                index++
                setUpChartData()
            }
        } else {
            observeVehiclesByPlate(start, end, label) {
                index++
                setUpChartData()
            }
        }

//        when (index) {
//            0 -> observeVehiclesByTariff(DateUtils.startOfToday(),
//                DateUtils.endOfToday(),
//                SpinnerLists.DATE_LIST[0]){
//                index++
//                setUpChartData()
//            } // Today by Tariff Name
//            1 -> observeVehiclesByPlate(
//                DateUtils.startOfToday(),
//                DateUtils.endOfToday(),
//                SpinnerLists.DATE_LIST[1]
//            ){
//                index++
//                setUpChartData()
//            } // Today by Plate Number
//            2 -> observeVehiclesByTariff(
//                DateUtils.startOfThisWeek(),
//                DateUtils.endOfThisWeek(),
//                SpinnerLists.DATE_LIST[2]
//            ){
//                index++
//                setUpChartData()
//            } // This Week by Tariff Name
//            3 -> observeVehiclesByPlate(
//                DateUtils.startOfThisWeek(),
//                DateUtils.endOfThisWeek(),
//                SpinnerLists.DATE_LIST[3]
//            ){
//                index++
//                setUpChartData()
//            } // This Week by Plate Number
//            4 -> observeVehiclesByTariff(
//                DateUtils.startOfLastWeek(),
//                DateUtils.endOfLastWeek(),
//                SpinnerLists.DATE_LIST[4]
//            ){
//                index++
//                setUpChartData()
//            } // Last Week by Tariff Name
//            5 -> observeVehiclesByPlate(
//                DateUtils.startOfLastWeek(),
//                DateUtils.endOfLastWeek(),
//                SpinnerLists.DATE_LIST[5]
//            ){
//                index++
//                setUpChartData()
//            } // Last Week by Plate Number
//            6 -> observeVehiclesByTariff(
//                DateUtils.startOfThisMonth(),
//                DateUtils.endOfThisMonth(),
//                SpinnerLists.DATE_LIST[6]
//            ){
//                index++
//                setUpChartData()
//            } // This Month by Tariff Name
//            7 -> observeVehiclesByPlate(
//                DateUtils.startOfThisMonth(),
//                DateUtils.endOfThisMonth(),
//                SpinnerLists.DATE_LIST[7]
//            ){
//                index++
//                setUpChartData()
//            } // This Month by Plate Number
//            8 -> observeVehiclesByTariff(
//                DateUtils.startOfThisYear(),
//                DateUtils.endOfThisYear(),
//                SpinnerLists.DATE_LIST[8]
//            ){
//                index++
//                setUpChartData()
//            } // This Year by Tariff Name
//            9 -> observeVehiclesByPlate(
//                DateUtils.startOfThisYear(),
//                DateUtils.endOfThisYear(),
//                SpinnerLists.DATE_LIST[9]
//            ){
//                index = 0
//                isPdfGenerateOn = false
//                openAlertDialog()
//            } // This Year by Plate Number
//        }
    }

    private fun openAlertDialog() {

        val builder = AlertDialog.Builder(requireContext()).setMessage("Do you want to download the report?").setPositiveButton("Yes"){ dialog, _ ->
            historyViewModel.getIncomeByPlateNumber().observe(viewLifecycleOwner){ summaryModels ->
                lifecycleScope.launch {
                    PDFHelper.generateReport(requireContext(), summaryModels, graphMap) { file ->
                        PDFHelper.openPDF(requireContext(), file)
                    }
                }
            }
        }.setNegativeButton("No"){ dialog, _ ->
            isPdfGenerateOn = false
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun observeVehiclesByTariff(start: LocalDate, end: LocalDate, title: String, onChartDataSetReady: () -> Unit) {
        binding.pieChart.data = null
        binding.pieChart.invalidate()
        historyViewModel.getHistoryBetweenForTariffName(start, end)
            .observe(viewLifecycleOwner) { summaryModels ->
                val pieEntry = mutableListOf<PieEntry>()

                val labels = mutableListOf<String>()
                summaryModels.forEach {
                    labels.add(it.name)
                    val price = it.totalIncome
                        .toBigDecimal()
                        .setScale(2, java.math.RoundingMode.HALF_UP)
                        .toFloat()
                    pieEntry.add(PieEntry(
                        price,
                        it.name
                    ))
                }

                val dataSet = PieDataSet(pieEntry, "Total Income $title").apply {
                    colors = pieEntry.mapIndexed { index, _ ->
                        colorPalette[index % colorPalette.size].toColorInt()
                    }
                    valueTextColor = Color.BLACK
                    valueTextSize = 12f
                    sliceSpace = 3f

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    }
                }

                if (summaryModels.isNotEmpty()) {
                    if (isPdfGenerateOn){
                        graphMap.put(title, dataSet)
                        onChartDataSetReady()
                    } else {
                        binding.pieChart.visibility = View.VISIBLE
                        binding.barChart.visibility = View.GONE
                        binding.pieChart.data = PieData(dataSet)
                        binding.pieChart.apply {
                            description.apply {
                                text = "Total Income by Tariffs"
                                textSize = 20f
                            }
                            isRotationEnabled = true
                            isDrawHoleEnabled = false
                            animateY(1000)
                            invalidate()
                        }
                    }
                } else {
                    if (isPdfGenerateOn){
                        onChartDataSetReady()
                    } else{
                        binding.pieChart.visibility = View.VISIBLE
                        binding.barChart.visibility = View.GONE
                        binding.barChart.clear()
                    }
                }
            }
    }

    private fun observeVehiclesByPlate(start: LocalDate, end: LocalDate, title: String, onChartDataSetReady: () -> Unit) {

        binding.barChart.data = null
        binding.barChart.invalidate()
        historyViewModel.getHistoryBetweenForPlate(start, end)
            .observe(viewLifecycleOwner) { summaryModels ->
                val dataList = mutableListOf<SummaryModel>()

                val labels = mutableListOf<String>()
                summaryModels.forEach {
                    dataList.add(it)
                    labels.add(it.name)
                }

                val barEntry = dataList.mapIndexed { index, history ->
                    BarEntry(index.toFloat(), history.totalIncome.toFloat())
                }

                val dataSet = BarDataSet(barEntry, "Total Income $title").apply {
                    colors = barEntry.mapIndexed { index, _ ->
                        colorPalette[index % colorPalette.size].toColorInt()
                    }
                    valueTextColor = Color.BLACK
                    valueTextSize = 12f

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    }
                }
                binding.barChart.data = BarData(dataSet).apply {
                    barWidth = 0.9f
                }

                binding.barChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(dataList.map {
                        it.name }
                    )
                    position = XAxis.XAxisPosition.BOTTOM
                    textSize = 12f
                    labelRotationAngle = -45f
                    granularity = 1f
                    setDrawGridLines(false)
                }


                if (summaryModels.isNotEmpty()) {
                    if (isPdfGenerateOn){
                        val dataSetLabelsArray = ChartDataLabels(labels, dataSet)
                        graphMap.put(title, dataSetLabelsArray)
                        onChartDataSetReady()
                    } else {
                        binding.barChart.visibility = View.VISIBLE
                        binding.pieChart.visibility = View.GONE
                        binding.barChart.apply {
                            data = BarData(dataSet)
                            description.apply {
                                text = "Total Income by Plate Number"
                                textSize = 20f
                            }
                            animateY(1000)
                            invalidate()
                        }
                    }
                } else {
                    if (isPdfGenerateOn){
                        onChartDataSetReady()
                    } else{
                        binding.barChart.visibility = View.VISIBLE
                        binding.pieChart.visibility = View.GONE
                        binding.barChart.clear()
                        // binding.barChart.setNoDataText("No data available for this range")
                    }
                }

            }
    }
}