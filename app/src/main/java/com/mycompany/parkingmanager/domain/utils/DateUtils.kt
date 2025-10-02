package com.mycompany.parkingmanager.domain.utils

import java.time.*

import java.time.*

object DateUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    fun startOfToday(): LocalDate {
        return LocalDate.now(zoneId)
    }

    fun endOfToday(): LocalDate {
        return LocalDate.now(zoneId)
    }

    fun startOfYesterday(): LocalDate {
        return LocalDate.now(zoneId).minusDays(1)
    }

    fun endOfYesterday(): LocalDate {
        return LocalDate.now(zoneId).minusDays(1)
    }

    fun startOfThisWeek(): LocalDate {
        return LocalDate.now(zoneId).with(DayOfWeek.MONDAY)
    }

    fun endOfThisWeek(): LocalDate {
        return LocalDate.now(zoneId)
            .with(DayOfWeek.MONDAY)
            .plusDays(6)
    }

    fun startOfLastWeek(): LocalDate {
        return LocalDate.now(zoneId)
            .with(DayOfWeek.MONDAY)
            .minusWeeks(1)
    }

    fun endOfLastWeek(): LocalDate {
        return LocalDate.now(zoneId)
            .with(DayOfWeek.MONDAY)
            .minusWeeks(1)
            .plusDays(6)
    }

    fun startOfThisMonth(): LocalDate {
        return LocalDate.now(zoneId).withDayOfMonth(1)
    }

    fun endOfThisMonth(): LocalDate {
        return LocalDate.now(zoneId)
            .withDayOfMonth(LocalDate.now(zoneId).lengthOfMonth())
    }

    fun startOfThisYear(): LocalDate{
        return LocalDate.now(zoneId).withDayOfYear(1)
    }

    fun endOfThisYear(): LocalDate{
        return LocalDate.now(zoneId).withDayOfYear(LocalDate.now(zoneId).lengthOfYear())
    }
}


