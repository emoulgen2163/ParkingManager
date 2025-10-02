package com.mycompany.parkingmanager.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.data.TariffDao
import com.mycompany.parkingmanager.data.VehicleDao
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.VehicleType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class VehicleRepository @Inject constructor(database: AppDatabase, @ApplicationContext private val context: Context) {

    private val vehicleDao = database.vehicleDao()

    private val contentResolver = context.contentResolver
    suspend fun addVehicle(vehicle: Vehicle) = vehicleDao.addVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    fun searchByPlate(plate: String): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = false)){
        vehicleDao.searchByPlate(plate)
    }.flow

    fun searchByEntryTime(startTime: Long, endTime: Long): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = false)){
        vehicleDao.searchByEntryTime(startTime, endTime)
    }.flow

    fun selectVehicleType(vehicleType: VehicleType): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = false)) {
        vehicleDao.selectVehicleType(vehicleType)
    }.flow

    fun getVehiclesByTariffName(): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = false)) {
        vehicleDao.getVehiclesByTariffName()
    }.flow

    fun getVehiclesByBrand(): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = false)) {
        vehicleDao.getVehiclesByBrand()
    }.flow

    fun observeAllVehicles(): Flow<PagingData<Vehicle>> = Pager(PagingConfig(pageSize = 1, enablePlaceholders = true)) {
        vehicleDao.observeAllVehicles()
    }.flow.map { pagingData ->
        pagingData.map { vehicle ->
            vehicle
        }
    }

    suspend fun exportToJsonFile(uri: Uri) = withContext(Dispatchers.IO) {
        val vehicles: List<Vehicle> = vehicleDao.getAllVehicles()
        val json = Json.encodeToString(vehicles)

        contentResolver.openOutputStream(uri)?.use { output ->
            output.write(json.toByteArray())
        }
    }

    suspend fun importFromJsonFile(uri: Uri) = withContext(Dispatchers.IO) {
        val json = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
            it.readText()
        }?.trim()

        if (!json.isNullOrEmpty()) {
            try {
                val vehicles: List<Vehicle> = Json.decodeFromString(json)
                vehicles.forEach {
                    vehicleDao.addVehicle(it)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // vehicleDao.insertAll(emptyList())
            Log.d("TAG", "Vehicle repository invalid JSON")
        }
    }



}