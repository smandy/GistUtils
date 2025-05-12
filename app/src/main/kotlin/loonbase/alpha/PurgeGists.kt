package loonbase.alpha

fun main() {
    println("Purging all pasted gists...")

    val gists = listGists()  // however you already call the API
    for (gist in gists) {
        if (gist.description.equals("Clipboard paste", true)) {
            println("Deleting ${gist.id} - ${gist.description}")
            deleteGist(gist.id)
        }
    }
    println("Purge complete.")
}
