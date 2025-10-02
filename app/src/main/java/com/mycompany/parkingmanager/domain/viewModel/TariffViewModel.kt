package com.mycompany.parkingmanager.domain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.repository.TariffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TariffViewModel @Inject constructor(private val repository: TariffRepository): ViewModel() {

    fun addTariff(tariff: Tariff) = viewModelScope.launch {
        repository.addTariff(tariff)
    }

    fun updateTariff(tariff: Tariff) = viewModelScope.launch {
        repository.updateTariff(tariff)
    }

    fun getTariff(tariffName: String) = repository.getTariff(tariffName)

    fun getTariffs() = repository.getTariffs()


}