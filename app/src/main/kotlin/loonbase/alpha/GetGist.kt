package loonbase.alpha

import importExport.gist.GistClipboardTool
//import loonbase.alpha.GistClipboardTool

fun main() {
    //val dest = System.getProperty("outFile") ?: error("Need property 'outFile'")
    GistClipboardTool.getMyLastGistAndSaveToFile()
}