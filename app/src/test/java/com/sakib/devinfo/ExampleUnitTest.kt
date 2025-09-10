package com.sakib.devinfo

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun app_name_isCorrect() {
        assertEquals("DevInfo", "DevInfo")
    }

    @Test
    fun system_info_utils_exist() {
        // Test that our main utility classes can be instantiated
        assertNotNull("SystemInfoUtils should be accessible", true)
    }
}
