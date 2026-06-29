package html4tree

import java.io.File
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
    val top_dir = File(topDir)
    require(top_dir.exists() && top_dir.isDirectory())

    val ll = LinkedList()

    ll.push(LinkedListEntry(top_dir,0))

    var lle: LinkedListEntry? = ll.pull()

    while(lle != null && lle.file.isDirectory()){
        val currentLevel: Int = lle.level
        if(maxLevel == -1 || currentLevel <= maxLevel)
           process_dir(lle.file)

        lle.file.listFiles().forEach {
            if(it.isDirectory()){
                ll.push( LinkedListEntry(it, currentLevel+1))
            }
        }
        lle = ll.pull()
    }
}

fun process_ignore_file(curr_dir: File): List<String> {

    val ignore_filename = ".html4ignore"
 
    val ignore_file_path = curr_dir.getAbsolutePath()+"/"+ignore_filename

    val ignore_file = File(ignore_file_path)

    val files_to_exclude = mutableListOf<String>()

    if(ignore_file.exists()){
       val ignored_strings = mutableListOf<String>()

       ignore_file.forEachLine { ignored_strings.add(it) }

       curr_dir.list().sorted().forEach {
           val current = it
           ignored_strings.forEach { i_string ->
              if(("^"+i_string+"$").toRegex().matches(current)){
                 files_to_exclude.add(current)
              }
         }
       }
    }

    if ("index.html" !in files_to_exclude)
       files_to_exclude.add("index.html")


    return files_to_exclude
}
 
fun String.escapeHtml(): String {
    return this.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
}

fun process_dir(curr_dir: File){
    
    val exclude: List<String> = process_ignore_file(curr_dir)

    val css = """
              <style>
              ul {
                list-style-type: none;
              }
              </style>
              """

    // 보안 강화: XSS 취약점 방지를 위해 디렉토리 및 파일 이름의 HTML 엔티티를 이스케이프 처리합니다.
    val safeDirName = curr_dir.getName().escapeHtml()

    val index_top = """<!doctype html>
<html>
     <head>
        <title>${safeDirName}</title>
        ${css}
     </head>
     <body>
       <h1>${safeDirName}</h1>
       <ul>
          <li><a style="display:block; width:100%" href="./..">&#x21B0; ..</a></li>
""" 

    val index_middle = fun():String{ 
        var l=""

        val dir_files: MutableList<File> = curr_dir.listFiles().toMutableList()
        dir_files.sortWith(compareBy ({it.name}) )
        dir_files.forEach {
           if((it.getName() !in exclude) && (it != curr_dir)) {
              val safeName = it.getName().escapeHtml()
              val href = if (it.isDirectory()) { "./${safeName}/" } else { "./${safeName}" }
              // 보안 강화: href 속성 값을 따옴표로 감싸서 속성 탈출(attribute breakout)을 방지합니다.
              l += """          <li><a style="display:block; width:100%" href="${href}">${if (it.isDirectory()) { "&#128193;" } else { "&rtrif;" }} ${safeName}</a></li>"""+"\n"
           }
        }

        return l;
     } 

   val index_bottom="""
       </ul>
    </body>
</html>
"""

   File(curr_dir,"index.html").writeText(index_top+index_middle()+index_bottom)

}

fun help() {
    println("ERROR: help has not been written yet!")
}
