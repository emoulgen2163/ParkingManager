package com.mycompany.parkingmanager.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repository: HistoryRepository): ViewModel() {

    fun insert(history: History){
        viewModelScope.launch {
            repository.insert(history)
        }
    }

    fun getHistoryBetweenForTariffName(startDate: LocalDate, endDate: LocalDate) = repository.getHistoryBetweenForTariffName(startDate, endDate)

    fun getHistoryBetweenForPlate(startDate: LocalDate, endDate: LocalDate) = repository.getHistoryBetweenForPlate(startDate, endDate)

    fun getIncomeByTariff() = repository.getIncomeByTariff()

    fun getIncomeByPlateNumber() = repository.getIncomeByPlateNumber()
}