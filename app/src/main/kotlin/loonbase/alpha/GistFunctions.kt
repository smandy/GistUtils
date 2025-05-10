package loonbase.alpha

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.readText

private val token: String by lazy {
    val props = Properties()
    val secretsFile = Path.of(System.getProperty("user.home"), "secrets.properties")
    props.load(secretsFile.inputStream())
    props.getProperty("github.token") ?: error("Missing github.token in secrets.properties")
}

private val httpClient = HttpClient.newHttpClient()
private val mapper = jacksonObjectMapper().registerKotlinModule()

data class GistFileContent(val content: String)
data class GistPayload(
    val description: String,
    val public: Boolean,
    val files: Map<String, GistFileContent>
)

fun createNewGistFromClipboardContent(
    filename: String = "snippet.kt",
    description: String = "Clipboard paste",
    isPublic: Boolean = false
) {
    val content = getClipboardText()
    if (content.isBlank()) {
        error("Clipboard is empty! Nothing to upload as gist.")
    }

    val payload = GistPayload(
        description = description,
        public = isPublic,
        files = mapOf(filename to GistFileContent(content))
    )

    val body = mapper.writeValueAsString(payload)

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/gists"))
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/vnd.github+json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    prettyPrint(response.body())
    println("Created gist: " + mapper.readTree(response.body()).get("html_url")?.asText())
}

fun listGists() : List<Gist> {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/gists?per_page=100"))
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/vnd.github+json")
        .GET()
        .build()

    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    //println("Response is ${response.body()}")
    //prettyPrint(response.body())
    val gists: List<Gist> = mapper.readValue(response.body())
    //println("Gists are ${gists.joinToString("\n")}")
    return gists
}

fun prettyPrint( s : String) : String {
    println("Parsing $s")
    val tmp = mapper.readValue(s, Object::class.java)
    val writer = mapper.writerWithDefaultPrettyPrinter()
    return writer.writeValueAsString(tmp)
    //writer.toSt
}

fun deleteGist(id : String) : Boolean {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/gists/$id"))
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/vnd.github+json")
        .DELETE()
        .build()
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    return response.statusCode()==204
}

fun getMyLastGist(): String? {
    val gists = listGists()
    val firstGist = gists.firstOrNull()
    val file = firstGist?.files?.values?.firstOrNull()

    return file?.let {
        val fileResponse = httpClient.send(
            HttpRequest.newBuilder().uri(URI.create(it.raw_url)).build(),
            HttpResponse.BodyHandlers.ofString()
        )
        fileResponse.body()
    }
}

private fun getClipboardText(): String {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return (clipboard.getData(DataFlavor.stringFlavor) as String).also { println(it) }
}

private fun String.toJsonString(): String =
    "\"" + this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""

@JsonIgnoreProperties(ignoreUnknown = true)
data class GistFile(val filename: String, val raw_url: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Gist(val id: String, val html_url: String, val description : String, val files: Map<String, GistFile>)



