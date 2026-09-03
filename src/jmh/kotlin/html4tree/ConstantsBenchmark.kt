package html4tree

import org.openjdk.jmh.annotations.*
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput, Mode.AverageTime)
open class ConstantsBenchmark {

    private lateinit var tempDir: File
    private lateinit var excludeArray: Array<String>
    private lateinit var excludeList: List<String>
    private lateinit var testFiles: Array<String>

    @Setup(Level.Trial)
    fun setup() {
        tempDir = Files.createTempDirectory("benchmark_dir").toFile()
        // Generate real workload: 10,000 files with a mix of extensions
        val exts = listOf(".txt", ".java", ".kt", ".md", ".json", ".xml", ".yaml", ".log", ".pcap", ".p12", ".sql")
        for (i in 0 until 10_000) {
            val ext = exts[i % exts.size]
            File(tempDir, "file_$$i$ext").createNewFile()
        }
        testFiles = tempDir.list() ?: emptyArray()

        val klass = Class.forName("html4tree.Constants")
        val listField = klass.getDeclaredField("defaultSensitiveExtensions")
        listField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        excludeList = listField.get(null) as List<String>

        val arrayField = klass.getDeclaredField("defaultSensitiveExtensionsArray")
        arrayField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        excludeArray = arrayField.get(null) as Array<String>
    }

    @TearDown(Level.Trial)
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Benchmark
    fun benchmarkListAny(): Int {
        var matchCount = 0
        for (fileName in testFiles) {
            val normalizedName = fileName.toLowerCase(java.util.Locale.ROOT)
            if (excludeList.any { normalizedName.endsWith(it) }) {
                matchCount++
            }
        }
        return matchCount
    }

    @Benchmark
    fun benchmarkArrayAny(): Int {
        var matchCount = 0
        for (fileName in testFiles) {
            val normalizedName = fileName.toLowerCase(java.util.Locale.ROOT)
            if (excludeArray.any { normalizedName.endsWith(it) }) {
                matchCount++
            }
        }
        return matchCount
    }
}
