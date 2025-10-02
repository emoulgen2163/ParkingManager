package com.mycompany.parkingmanager.domain.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty


object PreferenceKeys {
    val CURRENCY = stringPreferencesKey("currency")
    var TIME_FORMAT = stringPreferencesKey("time_format")
    val CURRENCY_CONVERTER_VALUES = stringPreferencesKey("currency_values")

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    // Extension: currency Flow
    val Context.currencyFlow: Flow<String> get() = dataStore.data.map { prefs ->
        prefs[CURRENCY] ?: "USD"
    }

    // Extension: time format Flow
    val Context.timeFormatFlow: Flow<String> get() = dataStore.data.map { prefs ->
        prefs[TIME_FORMAT] ?: "24h"
    }

    val Context.currencyValuesFlow: Flow<String> get() = dataStore.data.map { prefs ->
        prefs[CURRENCY_CONVERTER_VALUES] ?: ""
    }

    fun saveCurrency(context: Context, currencySymbol: String){
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { prefs ->
                prefs[CURRENCY] = currencySymbol
            }
        }
    }

    fun saveTimeFormat(context: Context, timeFormat: String){
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { prefs ->
                prefs[TIME_FORMAT] = timeFormat
            }
        }
    }

    fun saveCurrencyConverterValues(context: Context, currencyConverterValue: CurrencyConverter){
        val json = Gson().toJson(currencyConverterValue)
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { prefs ->
                prefs[CURRENCY_CONVERTER_VALUES] = json
            }
        }
    }


}