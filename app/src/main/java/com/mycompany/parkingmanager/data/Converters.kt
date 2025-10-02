package com.mycompany.parkingmanager.data

import androidx.room.TypeConverter
import com.mycompany.parkingmanager.domain.VehicleType
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromVehicleType(value: VehicleType): String {
        return value.name
    }

    @TypeConverter
    fun toVehicleType(value: String): VehicleType {
        return VehicleType.valueOf(value)
    }

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

}
