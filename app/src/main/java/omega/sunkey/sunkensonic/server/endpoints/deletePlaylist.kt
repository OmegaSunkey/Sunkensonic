package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun deletePlaylist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val playlistId = context.queryParam("id")
    if (playlistId == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        dao.deletePlaylist(playlistId)
        success(context, null)
    })
}