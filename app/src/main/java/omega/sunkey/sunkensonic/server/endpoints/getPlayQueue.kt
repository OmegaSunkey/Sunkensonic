package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.PlayQueueView
import omega.sunkey.sunkensonic.server.responses.SubsonicPlayQueue
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getPlayQueue(context: Context, scope: CoroutineScope, dao: SubsonicDao, playQueue: SubsonicPlayQueue) {
    context.future(scope.future {
        val queue = playQueue.getQueue(context.attribute<String>("currentUser")!!)
        /*if (queue.current == null) {
            reject(context, Reject.NODATA)
            return@future
        }*/

        for ((index, songdata) in queue.entry.withIndex()) {
            val song = dao.getSong(songdata.id)
            if (song == null) {
                playQueue.removeByIndexAndUsername(
                    index,
                    context.attribute<String>("currentUser")!!,
                    context.attribute<String>("client")!!
                )
            }
        }

        success(context, PlayQueueView(queue))
    })
}