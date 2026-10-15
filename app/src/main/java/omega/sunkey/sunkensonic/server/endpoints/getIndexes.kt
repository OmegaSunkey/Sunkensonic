package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.Index
import omega.sunkey.sunkensonic.server.responses.Indexes
import omega.sunkey.sunkensonic.server.responses.IndexesView
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.responses.toArtistData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.collections.set

fun getIndexes(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val musicFolderId = context.queryParam("musicFolderId")
    context.future(scope.future {
        val ignoredArticles = listOf("The", "El", "La", "Los", "Las")
        val allArtists = dao.getAllArtistsWithMeta()
        val allAlbums = dao.getAllAlbumsWithMeta()
        val indexArtistMap = mutableMapOf<String, MutableList<ArtistData>>()
        val indexAlbumMap = mutableMapOf<String, MutableList<AlbumData>>()
        val finalIndex = mutableListOf<Index>()
        for(metaartist in allArtists) {
            val fChar = getFirstChar(metaartist.artist.name, ignoredArticles)
            if(indexArtistMap.containsKey(fChar)) {
                indexArtistMap[fChar]!!.add(
                    metaartist.toArtistData()
                )
            } else {
                indexArtistMap[fChar] = mutableListOf(
                    metaartist.toArtistData()
                )
            }
        }
        for(metaalbum in allAlbums) {
            val fChar = getFirstChar(metaalbum.album.title, ignoredArticles)
            var totalDuration = 0
            metaalbum.songs.forEach {
                totalDuration += it.duration
            }
            if(indexAlbumMap.containsKey(fChar)) {
                indexAlbumMap[fChar]!!.add(
                    metaalbum.toAlbumData(totalDuration)
                )
            } else {
                indexAlbumMap[fChar] = mutableListOf(
                    metaalbum.toAlbumData(totalDuration)
                )
            }
        }
        for(index in (indexArtistMap.keys + indexAlbumMap.keys).distinct().sorted()) {
            finalIndex.add(
                Index(
                    index,
                    indexArtistMap[index].orEmpty(),
                    indexAlbumMap[index].orEmpty()
                )
            )
        }
        success(context, IndexesView(Indexes(ignoredArticles.joinToString(), finalIndex)))
    })
}
