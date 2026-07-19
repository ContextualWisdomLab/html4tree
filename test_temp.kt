import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun write_index_file(
    curr_dir: File,
    content: String,
    moveFile: (java.nio.file.Path, java.nio.file.Path, Array<java.nio.file.CopyOption>) -> java.nio.file.Path = { src, dest, options -> Files.move(src, dest, *options) }
) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        try {
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING))
        } catch (e: java.nio.file.AtomicMoveNotSupportedException) {
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.REPLACE_EXISTING))
        }
    } finally {
        Files.deleteIfExists(tempPath)
    }
}
