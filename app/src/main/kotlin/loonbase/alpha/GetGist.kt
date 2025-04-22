package loonbase.alpha

import importExport.gist.GistClipboardTool
//import loonbase.alpha.GistClipboardTool

fun main() {
    //val dest = System.getProperty("outFile") ?: error("Need property 'outFile'")
    val lastGist = GistClipboardTool.getMyLastGist() ?: error("No gist found")
    val outputFile = System.getProperty("output")
    if (outputFile != null) {
        File(outputFile).writeText(lastGist)
        println("Written to $outputFile")
    } else {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(lastGist), null)
        println("Copied to clipboard")
    }
}