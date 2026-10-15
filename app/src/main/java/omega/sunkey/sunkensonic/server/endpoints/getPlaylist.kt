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
import omega.sunkey.sunkensonic.server.responses.toSongData
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
        pl.songs?.forEach {
            songs.add(
                it.toSongData()
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