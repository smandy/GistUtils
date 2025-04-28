package loonbase.alpha

fun main() {
    println("Purging all gists...")

    val gists = listGists()  // however you already call the API
    for (gist in gists) {
        if (gist.description.equals("Clipboard paste", true)) {
            println("Deleting ${gist} ")
            val ret = deleteGist(gist.id)


            println("Ret is ${if(ret) "deleted" else "not deleted"}")
        }
    }
    println("Purge complete.")
}
