package com.mycompany.parkingmanager.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tariff(
    @PrimaryKey(true)
    val id: Long = 0,
    val tariffName: String,
    var price: Double,
    var currencySymbol: String = ""
)

data class SummaryModel(
    val id: Long,
    var name: String,
    var totalIncome: Double
)
