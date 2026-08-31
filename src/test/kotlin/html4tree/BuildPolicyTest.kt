package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class BuildPolicyTest {
    @Test
    fun jacocoCoverageGateRemainsComplete() {
        val buildFile = File("build.gradle")
        assertTrue(buildFile.isFile, "build.gradle must be available to the policy regression test")
        assertTrue(
            buildFile.readText().contains("minimum = 1.00"),
            "JaCoCo must continue to fail closed below 100% coverage"
        )
    }
}
