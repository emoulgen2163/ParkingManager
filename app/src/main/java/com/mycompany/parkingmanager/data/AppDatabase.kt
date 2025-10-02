package com.mycompany.parkingmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mycompany.parkingmanager.domain.History
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Vehicle::class, Tariff::class, History::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tariffDao(): TariffDao
    abstract fun historyDao(): HistoryDao


    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK){
            INSTANCE ?: createDatabase(context).also {
                INSTANCE = it
            }
        }


        fun createDatabase(context: Context): AppDatabase{
            val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "vehicle_db")
                .fallbackToDestructiveMigration(true)
                .build()

            INSTANCE = instance
            return instance
        }
    }
}