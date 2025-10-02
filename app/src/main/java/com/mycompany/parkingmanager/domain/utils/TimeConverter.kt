package com.mycompany.parkingmanager.domain.utils

class TimeConverter {

    fun millisToMinute(millisecond: Long): Long = millisecond / 1000 / 60
}