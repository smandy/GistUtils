package importExport.gist

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.writeText
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object GistClipboardTool {
    private val token: String by lazy {
        val props = Properties()
        val secretsFile = Path.of(System.getProperty("user.home"), "secrets.properties")
        Files.newInputStream(secretsFile).use { props.load(it) }
        props.getProperty("github.token") ?: error("Missing github.token in secrets.properties")
    }

    private val httpClient = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper().registerKotlinModule()

    fun createNewGistFromClipboardContent(
        filename: String = "snippet.kt",
        description: String = "Clipboard paste",
        isPublic: Boolean = false
    ) {
        val content = getClipboardText()
        val body = """
            {
              "description": "$description",
              "public": $isPublic,
              "files": {
                "$filename": {
                  "content": ${content.toJsonString()}
                }
              }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/gists"))
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        println("Created gist: " + mapper.readTree(response.body()).get("html_url")?.asText())
    }

    fun getMyLastGistAndSaveToFile() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/gists?per_page=100"))
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val gists: List<Gist> = mapper.readValue(response.body())

        val firstGist = gists.firstOrNull()
        val file = firstGist?.files?.values?.firstOrNull()

        if (file != null) {
            val outFile = Path.of(System.getProperty("outFile") ?: file.filename)
            val fileResponse = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(file.raw_url)).build(),
                HttpResponse.BodyHandlers.ofString()
            )
            outFile.writeText(fileResponse.body())
            println("✅ Saved ${file.filename} to $outFile")
        } else {
            println("❌ No file found in first gist.")
        }
    }

    private fun getClipboardText(): String {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        return clipboard.getData(DataFlavor.stringFlavor) as String
    }

    private fun String.toJsonString(): String =
        "\"" + this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GistFile(val filename: String, val raw_url: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Gist(val id: String, val html_url: String, val files: Map<String, GistFile>)
}

fun main() {
    // GistClipboardTool.createNewGistFromClipboardContent()
    // GistClipboardTool.getMyLastGistAndSaveToFile()
}