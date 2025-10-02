package com.mycompany.parkingmanager.domain

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleType {
    Car,
    SUV,
    Truck
}
