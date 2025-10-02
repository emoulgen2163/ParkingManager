package com.mycompany.parkingmanager

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.parkingmanager.domain.Tariff
import com.mycompany.parkingmanager.domain.utils.TimeConverter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // fun millisToMinute(millisecond: Long): Long = millisecond / 1000 / 60

    @Test
    fun millisToMinute(){
        val millisecond = 360000L
        val result = TimeConverter().millisToMinute(millisecond)
        println(result)
    }
}