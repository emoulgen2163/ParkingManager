package com.mycompany.parkingmanager.domain.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.VehicleType
import com.mycompany.parkingmanager.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(val repository: VehicleRepository): ViewModel() {

    private val _filter = MutableStateFlow<VehicleType?>(null)
    val filter: StateFlow<VehicleType?> = _filter

    fun addVehicle(vehicle: Vehicle){
        viewModelScope.launch {
            repository.addVehicle(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle){
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }


    fun searchByPlate(plate: String) = repository.searchByPlate(plate).cachedIn(viewModelScope)

    fun searchByEntryTime(startTime: Long, endTime: Long) = repository.searchByEntryTime(startTime, endTime).cachedIn(viewModelScope)

    fun getVehiclesByTariffName() = repository.getVehiclesByTariffName().cachedIn(viewModelScope)

    fun getVehiclesByBrand() = repository.getVehiclesByBrand().cachedIn(viewModelScope)

    val vehicle = filter.debounce(1000).distinctUntilChanged().flatMapLatest { type ->
        if (type == null){
            repository.observeAllVehicles()
        } else {
            repository.selectVehicleType(type)
        }
    }.cachedIn(viewModelScope)

    fun setFilter(type: VehicleType?) {
        _filter.value = type
    }

    fun exportVehicles(uri: Uri) {
        viewModelScope.launch {
            repository.exportToJsonFile(uri)
        }
    }

    fun importVehicles(uri: Uri) {
        viewModelScope.launch {
            repository.importFromJsonFile(uri)
        }
    }


}