package com.mycompany.parkingmanager.domain.utils

import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("latest")
    suspend fun getRates(@Query("base") base: String = "USD"): CurrencyResponse
}

data class CurrencyResponse(
    val rates: Map<String, Double>
)
