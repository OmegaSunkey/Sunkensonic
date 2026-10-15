package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.PlaylistData
import omega.sunkey.sunkensonic.server.responses.Playlists
import omega.sunkey.sunkensonic.server.responses.PlaylistsView
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getPlaylists(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val playlists = dao.getAllPlaylists()
        val playdata = mutableListOf<PlaylistData>()
        playlists.forEach {
            playdata.add(PlaylistData(
                it.id,
                it.name,
                it.comment,
                it.owner,
                it.public,
                it.songCount,
                it.duration,
                it.created?.toIsoTime(),
                it.changed?.toIsoTime()
            ))
        }
        success(context, PlaylistsView(Playlists(playdata)))
    })
}