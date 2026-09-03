package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.util.Locale

class ConstantsTest {
    @Test
    fun testSensitiveExtensionsArray() {
        val klass = Class.forName("html4tree.Constants")
        val field = klass.getDeclaredField("defaultSensitiveExtensionsArray")
        field.isAccessible = true
        val array = field.get(null) as Array<*>

        val listField = klass.getDeclaredField("defaultSensitiveExtensions")
        listField.isAccessible = true
        val list = listField.get(null) as List<*>

        assertEquals(list.size, array.size)
        list.forEachIndexed { index, item ->
            assertEquals(item, array[index])
        }
    }

    @Test
    fun testSensitiveExtensionsBehaviorRegression() {
        val klass = Class.forName("html4tree.Constants")
        val arrayField = klass.getDeclaredField("defaultSensitiveExtensionsArray")
        arrayField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val array = arrayField.get(null) as Array<String>

        val listField = klass.getDeclaredField("defaultSensitiveExtensions")
        listField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val list = listField.get(null) as List<String>

        val testCases = listOf(
            "test.pem", "test.PEM", "test.key", "test.KEY", "test.p12", "test.pfx", "test.crt", "test.cer", "test.der", "test.keystore", "test.truststore", "test.jks", "test.sqlite", "test.db", "test.bak", "test.sql", "test.pcap", "test.pcapng", "test.log", "test.swp", "test.swo", "test.swpx",
            // false cases
            "test.txt", "test.pem.txt", "test.jpg", "test", ".pem", ".key.jpg"
        )

        for (testCase in testCases) {
            val normalizedName = testCase.toLowerCase(Locale.ROOT)
            val listResult = list.any { normalizedName.endsWith(it) }
            val arrayResult = array.any { normalizedName.endsWith(it) }
            assertEquals("Behavior mismatch for $testCase", listResult, arrayResult)
        }
    }
}
