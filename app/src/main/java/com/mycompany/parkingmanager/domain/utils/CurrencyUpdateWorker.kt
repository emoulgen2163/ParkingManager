package com.mycompany.parkingmanager.domain.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

class CurrencyUpdateWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        return try {
            // Retrofit setup (you can inject instead of creating here)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.exchangerate.host/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(CurrencyApi::class.java)
            val response = api.getRates("USD")

            val rates = response.rates

            val values = hashMapOf(
                "DOLLAR_TO_EURO" to rates["EUR"],
                "EURO_TO_DOLLAR" to 1 / (rates["EUR"] ?: 1.0),
                "DOLLAR_TO_POUND" to rates["GBP"],
                "POUND_TO_DOLLAR" to 1 / (rates["GBP"] ?: 1.0),
                "EURO_TO_POUND" to (rates["GBP"] ?: 1.0) / (rates["EUR"] ?: 1.0),
                "POUND_TO_EURO" to (rates["EUR"] ?: 1.0) / (rates["GBP"] ?: 1.0)
            )

            // Save to Firestore
            firestore.collection("currency-conversion-values")
                .document("latest")
                .set(values)

            // Save locally (Preferences)
            val prefs = applicationContext.getSharedPreferences("currency", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong("last_update", System.currentTimeMillis())
                putFloat("DOLLAR_TO_EURO", values["DOLLAR_TO_EURO"]?.toFloat() ?: 0f)
                putFloat("EURO_TO_DOLLAR", values["EURO_TO_DOLLAR"]?.toFloat() ?: 0f)
                putFloat("DOLLAR_TO_POUND", values["DOLLAR_TO_POUND"]?.toFloat() ?: 0f)
                putFloat("POUND_TO_DOLLAR", values["POUND_TO_DOLLAR"]?.toFloat() ?: 0f)
                putFloat("EURO_TO_POUND", values["EURO_TO_POUND"]?.toFloat() ?: 0f)
                putFloat("POUND_TO_EURO", values["POUND_TO_EURO"]?.toFloat() ?: 0f)
                apply()
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
