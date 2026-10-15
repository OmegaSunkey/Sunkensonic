package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.SinglePlaylistData
import omega.sunkey.sunkensonic.server.responses.SinglePlaylistView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getPlaylist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val pl = dao.getPlaylist(id)
        val songs = mutableListOf<SongData>()
        if (pl == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        pl.songs?.forEach { val song = it.song
            songs.add(
                SongData(
                    id = song.id,
                    parent = song.albumId,
                    title = song.title,
                    album = it.album.title,
                    artist = it.artist.name,
                    track = song.track,
                    year = song.year,
                    genre = song.genre,
                    coverArt = song.coverArt,
                    size = song.size,
                    contentType = conType(song.suffix),
                    suffix = song.suffix,
                    duration = song.duration,
                    bitRate = song.bitrate,
                    path = song.path,
                    created = song.dateAdded.toIsoTime(),
                    albumId = song.albumId,
                    artistId = song.artistId,
                    userRating = song.userRating ?: 0,
                    starred = song.starred?.toIsoTime(),
                    playCount = song.playCount,
                    played = song.lastPlayed?.toIsoTime()
                )
            )
        }
        success(
            context,
            SinglePlaylistView(
                SinglePlaylistData(
                        pl.playlist.id,
                        pl.playlist.name,
                        pl.playlist.comment,
                        pl.playlist.owner,
                        pl.playlist.public,
                        pl.playlist.songCount,
                        pl.playlist.duration,
                        pl.playlist.created?.toIsoTime(),
                        pl.playlist.changed?.toIsoTime(),
                        songs
                )
            )
        )
    })
}