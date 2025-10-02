package com.mycompany.parkingmanager.domain.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.data.TariffDao
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.utils.CurrencyConverter
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TariffRepository @Inject constructor(private val database: AppDatabase) {

    private val dao = database.tariffDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addTariff(tariff: Tariff) = dao.addTariff(tariff)

    suspend fun updateTariff(tariff: Tariff) = dao.updateTariff(tariff)

    fun getTariff(tariffName: String) = dao.getTariff(tariffName)

    suspend fun getTariffFromFirebase(){
        val snapshot = firestore.collection("tariff").get().await()

        snapshot.forEach {
            val name = it.getString("tariffName")!!
            val price = it.getDouble("price")!!

            val tariff = Tariff(tariffName = name, price = price, currencySymbol = "$")
            val currentTariff = dao.getTariffByName(name)

            if (currentTariff == null) {
                dao.addTariff(tariff)
            } else {
                currentTariff.price = price
                currentTariff.currencySymbol = "$"
                dao.updateTariff(tariff)
            }
        }
    }

    suspend fun getCurrencyValuesFromFirebase(context: Context){
        val snapshot = firestore.collection("currency-conversion-values").get().await()

        if (!snapshot.isEmpty){
            val doc = snapshot.documents.first()
            CurrencyConverter.DOLLAR_TO_EURO = doc.getDouble("DOLLAR_TO_EURO")!!
            CurrencyConverter.EURO_TO_DOLLAR = doc.getDouble("EURO_TO_DOLLAR")!!
            CurrencyConverter.DOLLAR_TO_POUND = doc.getDouble("DOLLAR_TO_POUND")!!
            CurrencyConverter.POUND_TO_DOLLAR = doc.getDouble("POUND_TO_DOLLAR")!!
            CurrencyConverter.EURO_TO_POUND = doc.getDouble("EURO_TO_POUND")!!
            CurrencyConverter.POUND_TO_EURO = doc.getDouble("POUND_TO_EURO")!!

            PreferenceKeys.saveCurrencyConverterValues(context, CurrencyConverter)
        }
    }

    fun getTariffs() = dao.getTariffs()

}