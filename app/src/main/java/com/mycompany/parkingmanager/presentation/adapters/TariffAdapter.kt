package com.mycompany.parkingmanager.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.parkingmanager.databinding.ItemTariffBinding
import com.mycompany.parkingmanager.domain.Tariff

class TariffAdapter(private val onEditClicked: (Tariff) -> Unit): RecyclerView.Adapter<TariffAdapter.TariffViewHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<Tariff>(){
        override fun areItemsTheSame(
            oldItem: Tariff,
            newItem: Tariff
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Tariff,
            newItem: Tariff
        ): Boolean {
            return oldItem == newItem
        }
    }

    class TariffViewHolder(binding: ItemTariffBinding): RecyclerView.ViewHolder(binding.root){
        val tariffName = binding.tariffName
        val currencySymbol = binding.currencySymbol
        val price = binding.tariffPrice
        val edit = binding.editButton

    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TariffViewHolder {
        return TariffViewHolder(ItemTariffBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: TariffViewHolder,
        position: Int
    ) {
        val tariff = differ.currentList[position]

        holder.tariffName.text = tariff.tariffName
        holder.currencySymbol.text = tariff.currencySymbol
        holder.price.text = tariff.price.toString()

        holder.edit.setOnClickListener {
            onEditClicked(tariff)
        }

    }

    override fun getItemCount(): Int = differ.currentList.size

}