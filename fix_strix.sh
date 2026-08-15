cat << 'DIFF' > patch_strix.diff
--- src/main/kotlin/html4tree/main.kt
+++ src/main/kotlin/html4tree/main.kt
@@ -368,6 +368,14 @@
         Files.move(source, target, *options)
         Unit
     }
 ) {
+    // 🛡️ Sentinel: Re-validate that the target directory is not a symlink immediately before file operations
+    val attrs = try {
+        Files.readAttributes(curr_dir.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
+    } catch (e: Exception) {
+        null
+    }
+    if (attrs == null || attrs.isSymbolicLink || !attrs.isDirectory) return
+
     val indexPath = curr_dir.toPath().resolve("index.html")
     val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
DIFF
patch src/main/kotlin/html4tree/main.kt patch_strix.diff
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
./gradlew clean test
