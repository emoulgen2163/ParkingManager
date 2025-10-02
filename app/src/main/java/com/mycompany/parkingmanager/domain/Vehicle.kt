package com.mycompany.parkingmanager.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Vehicle(
    @PrimaryKey(true)
    val id: Int = 0,
    val plate: String,
    val brand: String,
    val vehicleType: VehicleType,
    val entryTime: Long = System.currentTimeMillis(),
    var exitTime: Long = 0,
    val tariffName: String
)
