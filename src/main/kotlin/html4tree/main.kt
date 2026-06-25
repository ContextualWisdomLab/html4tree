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

    // Security check: Do not crawl if the top directory itself is a symbolic link
    require(!java.nio.file.Files.isSymbolicLink(top_dir.toPath())) { "Top directory cannot be a symbolic link" }

    val ll = LinkedList()

    ll.push(LinkedListEntry(top_dir,0))

    var lle: LinkedListEntry? = ll.pull()

    while(lle != null && lle.file.isDirectory() && !java.nio.file.Files.isSymbolicLink(lle.file.toPath())){
        val currentLevel: Int = lle.level
        if(maxLevel == -1 || currentLevel <= maxLevel)
           process_dir(lle.file)

        lle.file.listFiles()?.forEach {
            if(it.isDirectory() && !java.nio.file.Files.isSymbolicLink(it.toPath())){
                ll.push( LinkedListEntry(it, currentLevel+1))
            }
        }
        lle = ll.pull()
    }
}

fun String.escapeHtml(): String {
    return this.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;")
               .replace("'", "&#x27;")
}

fun String.urlEncodePath(): String {
    return java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")
}

fun process_ignore_file(curr_dir: File): List<String> {

    val ignore_filename = ".html4ignore"
 
    val ignore_file_path = curr_dir.getAbsolutePath()+"/"+ignore_filename

    val ignore_file = File(ignore_file_path)

    val files_to_exclude = mutableListOf<String>()

    if(ignore_file.exists()){
       val ignored_regexes = mutableListOf<Regex>()

       ignore_file.forEachLine { ignored_regexes.add(("^"+it+"$").toRegex()) }

       curr_dir.list().sorted().forEach {
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
 
fun process_dir(curr_dir: File){
    
    val exclude: List<String> = process_ignore_file(curr_dir)

    val css = """
              <style>
              ul {
                list-style-type: none;
              }
              </style>
              """

    val index_top = """<!doctype html>
<html>
     <head>
        <title>${curr_dir.getName().escapeHtml()}</title>
        ${css}
     </head>
     <body>
       <h1>${curr_dir.getName().escapeHtml()}</h1>
       <ul>
          <li><a style="display:block; width:100%" href="./..">&#x21B0; ..</a></li>
""" 

    val index_middle = fun():String{ 
        var l=""

        val dir_files: MutableList<File> = curr_dir.listFiles().toMutableList()
        dir_files.sortWith(compareBy ({it.name}) )
        dir_files.forEach {
           if((it.getName() !in exclude) && (it != curr_dir)) {
              l += """          <li><a style="display:block; width:100%" href="${if (it.isDirectory()) { "./${it.getName().urlEncodePath()}/" } else { "./${it.getName().urlEncodePath()}" }}">${if (it.isDirectory()) { "&#128193;" } else { "&rtrif;" }} ${it.getName().escapeHtml()}</a></li>"""+"\n"
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
