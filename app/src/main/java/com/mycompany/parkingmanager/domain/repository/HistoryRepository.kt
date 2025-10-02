package com.mycompany.parkingmanager.domain.repository

import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.domain.History
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(val database: AppDatabase) {

    val historyDao = database.historyDao()

    suspend fun insert(history: History) = historyDao.insert(history)

    fun getHistoryBetweenForTariffName(startDate: LocalDate, endDate: LocalDate) = historyDao.getHistoryBetweenForTariffName(startDate, endDate)

    fun getHistoryBetweenForPlate(startDate: LocalDate, endDate: LocalDate) = historyDao.getHistoryBetweenForPlate(startDate, endDate)

    fun getIncomeByTariff() = historyDao.getIncomeByTariff()

    fun getIncomeByPlateNumber() = historyDao.getIncomeByPlateNumber()

}