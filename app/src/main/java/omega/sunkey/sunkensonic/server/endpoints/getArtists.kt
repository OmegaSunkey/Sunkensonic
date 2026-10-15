package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.ArtistIndex
import omega.sunkey.sunkensonic.server.responses.Artists
import omega.sunkey.sunkensonic.server.responses.ArtistsView
import omega.sunkey.sunkensonic.server.responses.toArtistData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getArtists(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val ignoredArticles = listOf("The", "El", "La", "Los", "Las")
    context.future(scope.future{
        val allArtists = dao.getAllArtistsWithMeta()
        val artistIndex = mutableListOf<ArtistIndex>()
        val artistMap = mutableMapOf<String, MutableList<ArtistData>>()
        for(metaartist in allArtists) {
            val fChar = getFirstChar(metaartist.artist.name, ignoredArticles)
            if(artistMap.containsKey(fChar)) {
                artistMap[fChar]!!.add(
                    metaartist.toArtistData()
                )
            } else {
                artistMap[fChar] = mutableListOf<ArtistData>(
                    metaartist.toArtistData()
                )
            }
        }
        for (artist in artistMap) {
            artistIndex.add(ArtistIndex(
                artist.key,
                artist.value
            ))
        }
        success(context, ArtistsView(Artists(ignoredArticles.joinToString(), artistIndex)))
    })
}

fun getFirstChar(name: String, igArt: List<String>): String {
    if(name.isEmpty()) return "#"

    var char = name.lowercase()
    for(art in igArt) {
        if(char.startsWith("$art ", ignoreCase = true)) {
            char = char.removePrefix("${art.lowercase()} ")
        }
    }
    char = char.first().uppercase()

    return if (!char.first().isLetter()) {
        "#"
    } else {
        char
    }
}