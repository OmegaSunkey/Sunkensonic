package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.SingleArtistView
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getArtist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaartist = dao.getArtistWithDetails(id)
        if (metaartist == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val albums = mutableListOf<AlbumData>()
        metaartist.albums.forEach { it ->
            var totalDuration = 0
            it.songs.forEach { s ->
                totalDuration += s.duration
            }
            albums.add(AlbumData(
                id = it.album.id,
                parent = it.album.artistId,
                album = it.album.title,
                title = it.album.title,
                name = it.album.title,
                coverArt = it.album.coverArt,
                songCount = it.songs.size,
                created = it.album.dateAdded.toIsoTime(),
                duration = totalDuration,
                playCount = it.album.playCount,
                played = it.album.lastPlayed?.toIsoTime(),
                artistId = metaartist.artist.id,
                artist = metaartist.artist.name,
                year = it.album.year ?: 2000,
                userRating = it.album.userRating ?: 0,
                starred = it.album.starred?.toIsoTime()
            ))
        }
        success(context, SingleArtistView(
            ArtistData(
                id = metaartist.artist.id,
                name = metaartist.artist.name,
                albumCount = metaartist.albums.size,
                coverArt = metaartist.albums[0].album.coverArt,
                userRating = metaartist.artist.userRating ?: 0,
                playCount = metaartist.artist.playCount,
                played = metaartist.artist.lastPlayed?.toIsoTime(),
                starred = metaartist.artist.starred?.toIsoTime(),
                album = albums
            )
        ))
    })
}