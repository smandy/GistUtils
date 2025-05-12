package loonbase.alpha

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

fun copyToClipboard(content: String) {
    val clipboardCommand = when {
        System.getenv("WAYLAND_DISPLAY") != null -> listOf("wl-copy") // For future!
        System.getenv("DISPLAY") != null -> listOf("xclip", "-selection", "clipboard")
        else -> null
    }

    clipboardCommand?.let { command ->
        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            process.outputStream.use { output ->
                output.write(content.toByteArray())
                output.flush()
            }

            process.waitFor()
        } catch (e: Exception) {
            println("Clipboard copy failed: ${e.message}")
        }
    } ?: run {
        // Windows
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(content), null)
        println("Copied to clipboard this should be a change ${clipboard}")
    }
}



fun main() {
    //val dest = System.getProperty("outFile") ?: error("Need property 'outFile'")
    val lastGist = getMyLastGist() ?: error("No gist found")
    println("LastGist is $lastGist")
    val outputFile = System.getProperty("output")
    if (outputFile != null) {
        File(outputFile).writeText(lastGist)
        println("Written to $outputFile")
    } else {
        copyToClipboard(lastGist)
    }

}
