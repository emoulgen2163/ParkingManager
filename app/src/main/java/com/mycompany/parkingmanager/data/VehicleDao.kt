package com.mycompany.parkingmanager.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.VehicleType

@Dao
interface VehicleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<Vehicle>)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM Vehicle WHERE id = :vehicleId")
    suspend fun getVehicleById(vehicleId: Int): Vehicle?

    @Query("SELECT * FROM Vehicle WHERE plate LIKE '%' || :plate || '%' ORDER BY entryTime DESC")
    fun searchByPlate(plate: String): PagingSource<Int, Vehicle>

    @Query("SELECT * FROM Vehicle WHERE entryTime BETWEEN :startTime AND :endTime ORDER BY entryTime ASC")
    fun searchByEntryTime(startTime: Long, endTime: Long): PagingSource<Int, Vehicle>

    @Query("SELECT * FROM Vehicle WHERE vehicleType = :vehicleType")
    fun selectVehicleType(vehicleType: VehicleType): PagingSource<Int, Vehicle>

    @Query("SELECT * FROM Vehicle ORDER BY tariffName ASC")
    fun getVehiclesByTariffName(): PagingSource<Int, Vehicle>

    @Query("SELECT * FROM Vehicle ORDER BY brand ASC")
    fun getVehiclesByBrand(): PagingSource<Int, Vehicle>

    @Query("SELECT * FROM Vehicle")
    fun getAllVehicles(): List<Vehicle>

    @Query("SELECT * FROM vehicle")
    fun observeAllVehicles(): PagingSource<Int, Vehicle> // For UI
}