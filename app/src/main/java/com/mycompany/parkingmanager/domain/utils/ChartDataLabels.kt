package com.mycompany.parkingmanager.domain.utils

import com.github.mikephil.charting.data.BarDataSet

data class ChartDataLabels(
    val labels: List<String>,
    val chartDataSet: BarDataSet
)
