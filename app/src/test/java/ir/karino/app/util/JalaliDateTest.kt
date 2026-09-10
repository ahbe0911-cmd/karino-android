package ir.karino.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliDateTest {
    @Test
    fun `converts Nowruz 1403 to Gregorian`() {
        assertEquals(GregorianDate(2024, 3, 20), JalaliDate(1403, 1, 1).toGregorian())
    }

    @Test
    fun `converts Gregorian date to Jalali`() {
        assertEquals(JalaliDate(1405, 6, 19), JalaliDate.fromGregorian(2026, 9, 10))
    }

    @Test
    fun `round trip keeps leap day`() {
        val jalali = JalaliDate(1399, 12, 30)
        val gregorian = jalali.toGregorian()
        assertTrue(jalali.isValid())
        assertEquals(
            jalali,
            JalaliDate.fromGregorian(gregorian.year, gregorian.month, gregorian.day),
        )
    }

    @Test
    fun `rejects invalid Esfand day`() {
        assertFalse(JalaliDate(1400, 12, 30).isValid())
    }
}
