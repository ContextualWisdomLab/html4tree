import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption

fun main() {
    val ignore_file = File(".html4ignore")
    ignore_file.createNewFile()
    println(ignore_file.isFile)
}
