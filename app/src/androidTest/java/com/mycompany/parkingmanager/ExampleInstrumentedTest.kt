package com.mycompany.parkingmanager

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.data.TariffDao
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.repository.TariffRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var tariffDao: TariffDao
    private lateinit var context: Context
    private lateinit var tariffRepository: TariffRepository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp(){
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        tariffDao = database.tariffDao()
        tariffRepository = TariffRepository(database)
    }

    @Test
    fun insertTariff() {
        runBlocking {
            val tariffA = Tariff(tariffName = "Tariff A", price = 3.0)
            val tariffB = Tariff(tariffName = "Tariff B", price = 6.0)
            val tariffC = Tariff(tariffName = "Tariff C", price = 9.0)
            val tariffD = Tariff(tariffName = "Tariff C", price = 10.0)


            tariffDao.addTariff(tariffA)
            tariffDao.addTariff(tariffB)
            tariffDao.addTariff(tariffC)
            tariffDao.addTariff(tariffD)

            val tariff = tariffDao.getTariff("Tariff A").getOrAwaitValue()
            val count = tariffDao.getTariffCount().getOrAwaitValue()
            val priceTariffGroup = tariffDao.getPriceByTariffGroup().getOrAwaitValue()

            Log.d("Test", "count: $count")
            Log.d("Test", "tariff: $tariff")
            Log.d("Test", "priceTariffGroup: $priceTariffGroup")

            // println(tariff)
        }
    }

    @Test
    fun getTariffFromFirebase() = runBlocking {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("tariff").get().await()

        snapshot.forEach {
            val name = it.getString("tariffName")!!
            val price = it.getDouble("price")!!

            val tariff = Tariff(tariffName = name, price = price)

            Log.d("Test", "tariff: $tariff")
        }
    }

    private fun <T> LiveData<T>.getOrAwaitValue(time: Long = 2, timeUnit: TimeUnit = TimeUnit.SECONDS): T {
        var data: T? = null
        val latch = CountDownLatch(1)

        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        if (!latch.await(time, timeUnit)) {
            //throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }


}