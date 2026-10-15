package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.SongView
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getSong(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metasong = dao.getSongWithMeta(id)
        if (metasong == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        success(context, SongView(
            SongData(
                metasong.song.id,
                metasong.album.id,
                false,
                metasong.song.title,
                metasong.album.title,
                metasong.artist.name,
                metasong.song.track,
                metasong.song.year,
                metasong.song.genre,
                metasong.song.coverArt,
                metasong.song.size,
                conType(metasong.song.suffix!!),
                metasong.song.suffix,
                metasong.song.userRating ?: 0,
                metasong.song.starred?.toIsoTime(),
                metasong.song.playCount,
                metasong.song.lastPlayed?.toIsoTime(),
                metasong.song.duration,
                metasong.song.bitrate,
                metasong.song.path,
                metasong.song.discNumber,
                metasong.song.dateAdded.toIsoTime(),
                metasong.song.albumId,
                metasong.song.artistId,
            )
        ))
    })
}
