package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.Search
import omega.sunkey.sunkensonic.server.responses.SearchView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.responses.toArtistData
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import kotlin.math.min

fun search3(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val query = context.queryParam("query")
    if (query == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val artistCount = if(context.queryParam("artistCount") != null) min(context.queryParam("artistCount")!!.toInt(), 40) else 20
    val albumCount = if(context.queryParam("albumCount") != null) min(context.queryParam("albumCount")!!.toInt(), 40) else 20
    val songCount = if(context.queryParam("songCount") != null) min(context.queryParam("songCount")!!.toInt(), 40) else 20

    val artistOffset = context.queryParam("artistOffset")?.toIntOrNull() ?: 0
    val albumOffset = context.queryParam("albumOffset")?.toIntOrNull() ?: 0
    val songOffset = context.queryParam("songOffset")?.toIntOrNull() ?: 0

    context.future(scope.future {
        val artists = dao.searchArtists(query, artistCount, artistOffset)
        val albums = dao.searchAlbums(query, albumCount, albumOffset)
        val songs = dao.searchSongs(query, songCount, songOffset)
        val metaartist = mutableListOf<ArtistData>()
        val metaalbum = mutableListOf<AlbumData>()
        val metasong = mutableListOf<SongData>()
        artists.forEach {
            metaartist.add(
                it.toArtistData()
            )
        }
        albums.forEach {
            var totalDuration = 0
            it.songs.forEach { s ->
                totalDuration += s.duration
            }
            metaalbum.add(
                it.toAlbumData(totalDuration)
            )
        }
        songs.forEach {
            metasong.add(
                it.toSongData()
            )
        }
        success(context, SearchView(Search(metaartist, metaalbum, metasong)))
    })
}
