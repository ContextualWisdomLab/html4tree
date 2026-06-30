package html4tree

import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int

class Html4tree : CliktCommand() {
    val maxLevel:Int by option(help="Number of levels deep for which to generate an index.html file", hidden = false).int().default(-1)
    val topDir: String by argument(help="Top directory to crawl")

    override fun run() {
        go(topDir, maxLevel)
    }
}

fun main(args: Array<String>)  = Html4tree().main(args)

fun go(topDir: String, maxLevel: Int)  {
    require(topDir.isNotBlank())
    val top_dir = File(topDir).canonicalFile
    require(Files.isDirectory(top_dir.toPath(), LinkOption.NOFOLLOW_LINKS)) { "Top directory must be an existing non-symlink directory" }

    val ll = LinkedList()

    ll.push(LinkedListEntry(top_dir,0))

    var lle: LinkedListEntry? = ll.pull()

    while(lle != null && Files.isDirectory(lle.file.toPath(), LinkOption.NOFOLLOW_LINKS)){
        val currentLevel: Int = lle.level
        if(maxLevel == -1 || currentLevel <= maxLevel)
           process_dir(lle.file)

        if(maxLevel == -1 || currentLevel < maxLevel) {
            lle.file.listFiles()?.forEach {
                if(Files.isDirectory(it.toPath(), LinkOption.NOFOLLOW_LINKS)){
                    ll.push( LinkedListEntry(it, currentLevel+1))
                }
            }
        }
        lle = ll.pull()
    }
}

// ⚡ Bolt: Optimize escapeHtml by replacing sequential .replace() calls with a single pass StringBuilder
// Reduces unnecessary string allocation and GC pressure, providing O(1) string creation for fast HTML rendering.
fun String.escapeHtml(): String {
    var sb: StringBuilder? = null
    for (i in indices) {
        val c = this[i]
        val replacement = when (c) {
            '&' -> "&amp;"
            '<' -> "&lt;"
            '>' -> "&gt;"
            '"' -> "&quot;"
            '\'' -> "&#x27;"
            '`' -> "&#x60;"
            else -> null
        }
        if (replacement != null) {
            if (sb == null) {
                sb = java.lang.StringBuilder(length + 16)
                sb.append(this, 0, i)
            }
            sb.append(replacement)
        } else {
            sb?.append(c)
        }
    }
    return sb?.toString() ?: this
}

fun String.urlEncodePath(): String {
    val encoded = StringBuilder()
    this.toByteArray(Charsets.UTF_8).forEach {
        val byte = it.toInt() and 0xff
        val isUnreserved = (byte in 'A'.toInt()..'Z'.toInt()) ||
                           (byte in 'a'.toInt()..'z'.toInt()) ||
                           (byte in '0'.toInt()..'9'.toInt()) ||
                           byte == '-'.toInt() ||
                           byte == '.'.toInt() ||
                           byte == '_'.toInt() ||
                           byte == '~'.toInt()
        if (isUnreserved) {
            encoded.append(byte.toChar())
        } else {
            encoded.append('%')
            encoded.append(byte.toString(16).padStart(2, '0').toUpperCase())
        }
    }
    return encoded.toString()
}

fun process_ignore_file(curr_dir: File): Set<String> {

    val ignore_filename = ".html4ignore"
 
    val ignore_file_path = curr_dir.getAbsolutePath()+"/"+ignore_filename

    val ignore_file = File(ignore_file_path)

    val files_to_exclude = mutableSetOf<String>()

    if(ignore_file.exists()){
       val ignored_regexes = mutableListOf<Regex>()

       ignore_file.forEachLine {
           val pattern = it.trim()
           if (pattern.isNotEmpty()) {
               try {
                   ignored_regexes.add(("^"+pattern+"$").toRegex())
               } catch (_: IllegalArgumentException) {
               }
           }
       }

       curr_dir.list()?.sorted()?.forEach {
           val current = it
           ignored_regexes.forEach { regex ->
              if(regex.matches(current)){
                 files_to_exclude.add(current)
              }
         }
       }
    }

    if ("index.html" !in files_to_exclude)
       files_to_exclude.add("index.html")

    return files_to_exclude
}

fun write_index_file(curr_dir: File, content: String) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        Files.move(tempPath, indexPath, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(tempPath)
    }
}
 
fun process_dir(curr_dir: File){
    
    val exclude: Set<String> = process_ignore_file(curr_dir)

    val css = """
              <style>
              ul {
                list-style-type: none;
                padding-left: 0;
              }
              a {
                padding: 0.5rem;
                text-decoration: none;
                color: #0366d6;
                border-radius: 4px;
              }
              a:hover, a:focus-visible {
                background-color: #f6f8fa;
                text-decoration: underline;
                outline: 2px solid #0366d6;
                outline-offset: -2px;
              }
              </style>
              """

    val index_top = """<!doctype html>
<html lang="ko">
     <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <!-- 보안 향상: 인라인 스크립트 실행 방지 -->
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline';">
        <title>${curr_dir.getName().escapeHtml()}</title>
        ${css}
     </head>
     <body>
       <main>
         <h1>${curr_dir.getName().escapeHtml()}</h1>
         <nav aria-label="Directory listing">
         <ul>
            <li><a style="display:block; width:100%" href="./.." aria-label="상위 디렉토리로 이동"><span aria-hidden="true">&#x21B0;</span> ..</a></li>
""" 

    val index_middle = fun():String{ 
        val l = StringBuilder()

        val dir_files: MutableList<File> = curr_dir.listFiles()?.toMutableList() ?: mutableListOf()
        dir_files.sortWith(compareBy ({it.name}) )
        dir_files.forEach {
           val isLinkedDirectory = Files.isDirectory(it.toPath(), LinkOption.NOFOLLOW_LINKS)
           if((it.getName() !in exclude) && (isLinkedDirectory || !it.isDirectory()) && !Files.isSymbolicLink(it.toPath())) {
              val fileName = it.getName()
              val encodedHref = if (isLinkedDirectory) { "./${fileName.urlEncodePath()}/" } else { "./${fileName.urlEncodePath()}" }
              val ariaLabel = "${fileName} ${if (isLinkedDirectory) { "디렉토리" } else { "파일" }}".escapeHtml()
              val icon = if (isLinkedDirectory) { "&#128193;" } else { "&rtrif;" }
              l.append("""          <li><a style="display:block; width:100%" href="${encodedHref}" aria-label="${ariaLabel}"><span aria-hidden="true">${icon}</span> ${fileName.escapeHtml()}</a></li>""")
              l.append('\n')
           }
        }

        if(l.isEmpty()){
            l.append("""          <li><div style="padding: 0.5rem; color: #666; font-style: italic;">이 디렉토리는 비어 있습니다.</div></li>""")
            l.append('\n')
        }

        return l.toString();
     } 

   val index_bottom="""
         </ul>
         </nav>
       </main>
    </body>
</html>
"""

   write_index_file(curr_dir, index_top+index_middle()+index_bottom)

}

fun help() {
    println("ERROR: help has not been written yet!")
}
