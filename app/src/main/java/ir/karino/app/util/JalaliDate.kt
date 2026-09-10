package ir.karino.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GregorianDate(val year: Int, val month: Int, val day: Int)

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    fun toGregorian(): GregorianDate {
        var jy = year + 1595
        var days = -355668L + (365L * jy) + ((jy / 33) * 8L) + ((jy % 33 + 3) / 4L)
        days += day + if (month < 7) (month - 1) * 31L else (month - 7) * 30L + 186L

        var gy = 400 * (days / 146097).toInt()
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524).toInt()
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461).toInt()
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365).toInt()
            days = (days - 1) % 365
        }

        var gd = days.toInt() + 1
        val monthDays = intArrayOf(
            0,
            31,
            if (isGregorianLeap(gy)) 29 else 28,
            31,
            30,
            31,
            30,
            31,
            31,
            30,
            31,
            30,
            31,
        )
        var gm = 1
        while (gm <= 12 && gd > monthDays[gm]) {
            gd -= monthDays[gm]
            gm++
        }
        return GregorianDate(gy, gm, gd)
    }

    fun isValid(): Boolean {
        if (year !in 1..3177 || month !in 1..12 || day !in 1..31) return false
        return runCatching {
            val gregorian = toGregorian()
            fromGregorian(gregorian.year, gregorian.month, gregorian.day) == this
        }.getOrDefault(false)
    }

    fun daysInMonth(): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (copy(day = 30).isValid()) 30 else 29
        else -> 0
    }

    fun toEpochMillis(
        hour: Int,
        minute: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        require(isValid()) { "تاریخ شمسی نامعتبر است." }
        require(hour in 0..23 && minute in 0..59) { "ساعت نامعتبر است." }
        val gregorian = toGregorian()
        return LocalDateTime.of(
            gregorian.year,
            gregorian.month,
            gregorian.day,
            hour,
            minute,
        ).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun format(): String = "$day ${MONTH_NAMES[month - 1]} $year".toPersianDigits()

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین",
            "اردیبهشت",
            "خرداد",
            "تیر",
            "مرداد",
            "شهریور",
            "مهر",
            "آبان",
            "آذر",
            "دی",
            "بهمن",
            "اسفند",
        )

        fun today(zoneId: ZoneId = ZoneId.systemDefault()): JalaliDate {
            val date = LocalDate.now(zoneId)
            return fromGregorian(date.year, date.monthValue, date.dayOfMonth)
        }

        fun fromEpochMillis(
            epochMillis: Long,
            zoneId: ZoneId = ZoneId.systemDefault(),
        ): JalaliDate {
            val date = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
            return fromGregorian(date.year, date.monthValue, date.dayOfMonth)
        }

        fun fromGregorian(year: Int, month: Int, day: Int): JalaliDate {
            val cumulativeDays = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            val adjustedYear = if (month > 2) year + 1 else year
            var days = 355666L + (365L * year) + ((adjustedYear + 3) / 4L)
            days -= ((adjustedYear + 99) / 100L)
            days += ((adjustedYear + 399) / 400L) + day + cumulativeDays[month - 1]

            var jalaliYear = -1595 + 33 * (days / 12053).toInt()
            days %= 12053
            jalaliYear += 4 * (days / 1461).toInt()
            days %= 1461
            if (days > 365) {
                jalaliYear += ((days - 1) / 365).toInt()
                days = (days - 1) % 365
            }
            val jalaliMonth: Int
            val jalaliDay: Int
            if (days < 186) {
                jalaliMonth = 1 + (days / 31).toInt()
                jalaliDay = 1 + (days % 31).toInt()
            } else {
                jalaliMonth = 7 + ((days - 186) / 30).toInt()
                jalaliDay = 1 + ((days - 186) % 30).toInt()
            }
            return JalaliDate(jalaliYear, jalaliMonth, jalaliDay)
        }

        private fun isGregorianLeap(year: Int): Boolean =
            year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}

fun Long.formatJalaliDateTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val zoned = Instant.ofEpochMilli(this).atZone(zoneId)
    val jalali = JalaliDate.fromGregorian(
        zoned.year,
        zoned.monthValue,
        zoned.dayOfMonth,
    )
    val time = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT).format(zoned)
    return "${jalali.format()}، $time".toPersianDigits()
}

fun Long.isSameLocalDay(other: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate() ==
        Instant.ofEpochMilli(other).atZone(zoneId).toLocalDate()

fun String.toPersianDigits(): String = buildString(length) {
    this@toPersianDigits.forEach { character ->
        append(
            when (character) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> character
            },
        )
    }
}
