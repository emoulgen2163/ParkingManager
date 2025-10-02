package com.mycompany.parkingmanager.domain.utils

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.domain.repository.TariffRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import javax.inject.Inject

class SystemPeriodicWorker (appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {

    private val database = AppDatabase.invoke(applicationContext)
    private val repository = TariffRepository(database)

    override suspend fun doWork(): Result {
        Log.d("MyPeriodicWorker", "Periodic task is running")

        // Simulate some work
        NotificationManager().scheduleDailyNotification(applicationContext)
        // NotificationManager().showNotification(applicationContext, "Parking Manager", "Tariff Updated", null)
        repository.getTariffFromFirebase()
        repository.getCurrencyValuesFromFirebase(applicationContext)


        // Return success/failure/retry
        return Result.success()

    }



}