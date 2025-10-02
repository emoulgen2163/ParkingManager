package com.mycompany.parkingmanager.presentation.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.mycompany.parkingmanager.R
import com.mycompany.parkingmanager.databinding.ActivityMainBinding
import com.mycompany.parkingmanager.domain.utils.NotificationManager
import com.mycompany.parkingmanager.domain.utils.SystemPeriodicWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import com.google.gson.Gson
import com.mycompany.parkingmanager.domain.utils.CurrencyConverter
import com.mycompany.parkingmanager.domain.utils.CurrencyUpdateWorker
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.currencyValuesFlow
import com.mycompany.parkingmanager.domain.utils.PreferenceKeys.timeFormatFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if (isGranted){
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        NotificationManager().createNotificationChannel(this)
        NotificationManager().scheduleDailyNotification(this)
        checkNotificationPermission()

        scheduleCurrencyWorker(applicationContext)

        schedulePeriodicWork(this)
        sharedPreferencesSetup()
        printPrefsFile(applicationContext)

    }

    private fun scheduleCurrencyWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<CurrencyUpdateWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // only run if online
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "currency_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            when{
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // NotificationManager().showNotification(this, "", "")
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun schedulePeriodicWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<SystemPeriodicWorker>(23, TimeUnit.HOURS) // repeat interval
            .setInitialDelay(0, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) // only when connected to network
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("MyPeriodicWork", ExistingPeriodicWorkPolicy.UPDATE, workRequest)
        Log.d("MyPeriodicWorker", "Periodic task is running")

    }

    private fun sharedPreferencesSetup(){
        val masterKeyAlliance = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            "secure-prefs",
            masterKeyAlliance,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs.edit {
            putString("user-email", FirebaseAuth.getInstance().currentUser?.email)
        }

        prefs.getString("user-email", null)
    }

    fun printPrefsFile(context: Context) {
        // Path to your EncryptedSharedPreferences file
        val prefsFile = File(context.filesDir.parent!! + "/shared_prefs/secure-prefs.xml")

        if (prefsFile.exists()) {
            val content = prefsFile.readText()
            Log.d("EncryptedSharedPreferences", "EncryptedSharedPreferences file content:\n$content")
        } else {
            Log.d("EncryptedSharedPreferences", "File does not exist!")
        }
    }

//    fun setUpCurrencyConverterValues(){
//        CoroutineScope(Dispatchers.IO).launch {
//            val currencyValueStr = currencyValuesFlow.first()
//
//            if (currencyValueStr.isNotEmpty()) {
//                val currencyValue = Gson().fromJson(currencyValueStr, CurrencyConverter::class.java)
//                CurrencyConverter.DOLLAR_TO_EURO = currencyValue.DOLLAR_TO_EURO
//                CurrencyConverter.EURO_TO_DOLLAR = currencyValue.EURO_TO_DOLLAR
//
//                CurrencyConverter.POUND_TO_DOLLAR = currencyValue.POUND_TO_DOLLAR
//                CurrencyConverter.DOLLAR_TO_POUND = currencyValue.DOLLAR_TO_POUND
//
//                CurrencyConverter.POUND_TO_EURO = currencyValue.POUND_TO_EURO
//                CurrencyConverter.EURO_TO_POUND = currencyValue.EURO_TO_POUND
//            }
//
//
//        }
//  }


}