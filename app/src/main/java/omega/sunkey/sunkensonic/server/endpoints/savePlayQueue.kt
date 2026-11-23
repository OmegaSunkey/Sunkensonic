package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.SubsonicPlayQueue
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun savePlayQueue(context: Context, scope: CoroutineScope, dao: SubsonicDao, playQueue: SubsonicPlayQueue) {
    val ids = context.queryParams("id")
    val current = context.queryParam("current")
    val position = context.queryParam("position")?.toLongOrNull() ?: 0
    val user = context.attribute<String>("currentUser")!!

    if (ids.isNotEmpty() && current == null) {
        reject(context, Reject.MISSINGPARAM)
    }

    context.future(scope.future {
        playQueue.clearQueueByUsername(user, context.attribute<String>("client")!!)
        if (ids.isEmpty()) {
            success(context, null)
            return@future
        }

        for (id in ids) {
            val song = dao.getSongWithMeta(id)?.toSongData()
            if(song != null)
                playQueue.addToQueueByUsername(
                    song,
                    user,
                    context.attribute<String>("client")!!
                )
        }

        if(current != null) playQueue.setCurrentByUsername(current, position, user)

        success(context, null)
    })
}