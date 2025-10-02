package com.mycompany.parkingmanager.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.mycompany.parkingmanager.databinding.VehicleItemBinding
import com.mycompany.parkingmanager.domain.Vehicle
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.timeFormatFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehicleListAdapter(private val onVehicleClick: (Vehicle) -> Unit): PagingDataAdapter<Vehicle, VehicleListAdapter.VehicleViewHolder>(differCallBack) {

    companion object {
        private val differCallBack = object : DiffUtil.ItemCallback<Vehicle>(){
            override fun areItemsTheSame(
                oldItem: Vehicle,
                newItem: Vehicle
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.vehicleType.name == newItem.vehicleType.name
            }

            override fun areContentsTheSame(
                oldItem: Vehicle,
                newItem: Vehicle
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    class VehicleViewHolder(
        private val binding: VehicleItemBinding,
        val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle, onVehicleClick: (Vehicle) -> Unit) {
            // Collect time format on the main thread
            CoroutineScope(Dispatchers.Main).launch {
                val timeFormat = context.timeFormatFlow.first()
                val dateFormat = when (timeFormat) {
                    "12h" -> SimpleDateFormat("hh:mm a", Locale.getDefault())
                    "24h" -> SimpleDateFormat("HH:mm", Locale.getDefault())
                    else -> SimpleDateFormat("HH:mm", Locale.getDefault())
                }
                binding.entryTime.text =
                    "Time of Entry: ${dateFormat.format(Date(vehicle.entryTime))}"
            }

            binding.plateNo.text = "Plate Number: ${vehicle.plate}"
            binding.brand.text = "Brand: ${vehicle.brand}"
            binding.vehicleType.text = "Vehicle Type: ${vehicle.vehicleType.name}"
            binding.tariffName.text = "Tariff: ${vehicle.tariffName}"

            binding.button2.setOnClickListener { onVehicleClick(vehicle) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = VehicleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VehicleViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = getItem(position) ?: return
        holder.bind(vehicle, onVehicleClick)
    }
}