package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import kotlin.time.Clock

fun updatePlaylist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val playlistId = context.queryParam("playlistId")
    if (playlistId == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val name = context.queryParam("name")
    val comment = context.queryParam("comment")
    val public = context.queryParam("public")
    val songIds = context.queryParams("songIdToAdd")
    val removeSongs = context.queryParams("songIndexToRemove")

    context.future(scope.future {
        val playlist = dao.getPlaylist(playlistId)
        if (playlist == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        if (songIds.isNotEmpty()) {
            val (_, songPlaylist, _) = getSongList(songIds, dao, playlist.playlist.name, playlist.songs?.size)
            dao.insertPlaylistSongs(songPlaylist)
            dao.updatePlaylistDate(playlistId, Clock.System.now().epochSeconds)
            success(context, null)
        } else if(removeSongs.isNotEmpty()) {
            val songs = mutableListOf<Int>()
            removeSongs.forEach {
                songs.add(it.toInt())
            }
            dao.deleteSongsByIndex(playlistId, songs)
            dao.updatePlaylistDate(playlistId, Clock.System.now().epochSeconds)
            success(context, null)
        } else {
            name?.let { dao.updatePlaylistName(playlistId,it); dao.updatePlaylistDate(playlistId, Clock.System.now().epochSeconds) }
            comment?.let { dao.updatePlaylistComment(playlistId, it); dao.updatePlaylistDate(playlistId, Clock.System.now().epochSeconds) }
            public?.let { dao.updatePlaylistPublic(playlistId, it.toBoolean()); dao.updatePlaylistDate(playlistId, Clock.System.now().epochSeconds) }
        }
    })
}