import java.nio.file.Files
import java.io.File

fun main() {
    val tempDir = Files.createTempDirectory("test").toFile()
    val ignoreDir = File(tempDir, ".html4ignore")
    ignoreDir.mkdir()
    println("Done")
}
