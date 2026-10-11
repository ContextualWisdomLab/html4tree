import java.nio.file.Files
import java.io.File
import java.nio.file.attribute.PosixFilePermissions

fun main() {
    val tempPath = Files.createTempFile(File(".").toPath(), ".index-", ".html")
    println(Files.getPosixFilePermissions(tempPath))
    Files.deleteIfExists(tempPath)
}
