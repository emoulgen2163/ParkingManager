package com.mycompany.parkingmanager.domain.dependencyInjection

import android.content.Context
import com.mycompany.parkingmanager.data.AppDatabase
import com.mycompany.parkingmanager.data.HistoryDao
import com.mycompany.parkingmanager.data.TariffDao
import com.mycompany.parkingmanager.data.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return AppDatabase.invoke(context)
    }

    @Provides
    @Singleton
    fun provideVehicleDao(appDatabase: AppDatabase): VehicleDao{
        return appDatabase.vehicleDao()
    }

    @Provides
    @Singleton
    fun provideTariffDao(appDatabase: AppDatabase): TariffDao{
        return appDatabase.tariffDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(appDatabase: AppDatabase): HistoryDao{
        return appDatabase.historyDao()
    }
}