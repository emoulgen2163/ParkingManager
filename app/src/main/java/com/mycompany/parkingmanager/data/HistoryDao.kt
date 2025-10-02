package com.mycompany.parkingmanager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.domain.SummaryModel
import java.time.LocalDate

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: History): Long

    @Query("SELECT id, tariffName AS name, SUM(fee) AS totalIncome FROM History WHERE entryDate BETWEEN :startDate AND :endDate GROUP BY tariffName")
    fun getHistoryBetweenForTariffName(startDate: LocalDate, endDate: LocalDate): LiveData<List<SummaryModel>>

    @Query("SELECT id, plate AS name, SUM(fee) AS totalIncome FROM History WHERE entryDate BETWEEN :startDate AND :endDate GROUP BY plate")
    fun getHistoryBetweenForPlate(startDate: LocalDate, endDate: LocalDate): LiveData<List<SummaryModel>>

    @Query("SELECT id, tariffName AS name, SUM(fee) AS totalIncome FROM History GROUP BY tariffName")
    fun getIncomeByTariff(): LiveData<List<SummaryModel>>

    @Query("SELECT id, plate AS name, SUM(fee) AS totalIncome FROM History GROUP BY plate")
    fun getIncomeByPlateNumber(): LiveData<List<SummaryModel>>



}