package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildPolicyTest {
    private fun extractGradleBlock(script: String, blockName: String): String {
        val markerIndex = script.indexOf(blockName)
        assertTrue(markerIndex >= 0, "$blockName must exist in build.gradle")
        val openBrace = script.indexOf('{', markerIndex)
        assertTrue(openBrace >= 0, "$blockName must declare a block")

        var depth = 0
        for (index in openBrace until script.length) {
            when (script[index]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return script.substring(openBrace + 1, index)
                    }
                }
            }
        }
        throw AssertionError("$blockName must have balanced braces")
    }

    @Test
    fun jacocoCoverageGateRemainsComplete() {
        val buildFile = File("build.gradle")
        assertTrue(buildFile.isFile, "build.gradle must be available to the policy regression test")

        val coverageBlock = extractGradleBlock(
            buildFile.readText(),
            "jacocoTestCoverageVerification"
        )
        val activeThresholds = Regex("""(?m)^\s*minimum\s*=\s*([0-9]+(?:\.[0-9]+)?)\s*$""")
            .findAll(coverageBlock)
            .map { it.groupValues[1] }
            .toList()

        assertEquals(
            listOf("1.00"),
            activeThresholds,
            "JaCoCo must have exactly one active 100% coverage threshold"
        )
    }
}
