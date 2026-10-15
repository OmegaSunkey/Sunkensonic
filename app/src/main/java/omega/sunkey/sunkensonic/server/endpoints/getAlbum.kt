package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getAlbum(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaalbum = dao.getAlbumWithSongs(id)
        val songList = mutableListOf<SongData>()
        if (metaalbum == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        var totalDuration = 0
        metaalbum.songs.forEach { it ->
            totalDuration += it.duration
            songList.add(SongData(
                id = it.id,
                parent = it.albumId,
                title = it.title,
                album = metaalbum.album.title,
                artist = metaalbum.artist.name,
                track = it.track,
                discNumber = it.discNumber,
                year = it.year,
                genre = it.genre,
                coverArt = it.coverArt,
                size = it.size,
                contentType = conType(it.suffix),
                suffix = it.suffix,
                duration = it.duration,
                bitRate = it.bitrate,
                path = it.path,
                created = it.dateAdded.toIsoTime(),
                albumId = it.albumId,
                artistId = it.artistId
            ))
        }
        success(context, AlbumView(
            AlbumData(
                id = metaalbum.album.id,
                parent = metaalbum.album.artistId,
                album = metaalbum.album.title,
                coverArt = metaalbum.album.coverArt,
                songCount = metaalbum.songs.size,
                created = metaalbum.album.dateAdded.toIsoTime(),
                duration = totalDuration,
                playCount = metaalbum.album.playCount,
                played = metaalbum.album.lastPlayed?.toIsoTime(),
                artistId = metaalbum.album.artistId,
                artist = metaalbum.artist.name,
                year = metaalbum.album.year,
                genre = metaalbum.songs[0].genre ?: "Unknown Genre",
                song = songList,
                userRating = metaalbum.album.userRating ?: 0,
                starred = metaalbum.album.starred?.toIsoTime()
            )
        ))
    })
}
