package com.mycompany.parkingmanager.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.mikephil.charting.data.BarDataSet
import java.time.LocalDate

@Entity
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plate: String,
    val vehicleType: VehicleType,
    val entryTime: Long,
    val exitTime: Long,
    val entryDate: LocalDate,
    val exitDate: LocalDate,
    val tariffName: String,
    val fee: Double
)