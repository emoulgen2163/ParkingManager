package com.mycompany.parkingmanager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.SummaryModel

@Dao
interface TariffDao {

    @Insert
    suspend fun addTariff(tariff: Tariff)

    @Query("SELECT * FROM Tariff WHERE tariffName = :tariffName LIMIT 1")
    fun getTariff(tariffName: String): LiveData<Tariff>

    @Update
    suspend fun updateTariff(tariff: Tariff)

    @Query("SELECT * FROM Tariff")
    fun getTariffs(): LiveData<List<Tariff>>

    @Query("SELECT * FROM Tariff WHERE tariffName = :tariffName")
    fun getTariffByName(tariffName: String): Tariff? // for firebase

    @Query("SELECT id, tariffName AS name, SUM(price) AS totalIncome FROM Tariff GROUP BY tariffName")
    fun getPriceByTariffGroup(): LiveData<List<SummaryModel>>

    @Query("SELECT COUNT(*) FROM Tariff")
    fun getTariffCount(): LiveData<Int>


}