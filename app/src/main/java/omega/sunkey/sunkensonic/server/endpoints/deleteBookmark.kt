package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.SubsonicPlayQueue
import omega.sunkey.sunkensonic.server.success

fun deleteBookmark(context: Context, scope: CoroutineScope, playQueue: SubsonicPlayQueue) {
    val id = context.queryParam("id")
    if (id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val user = context.attribute<String>("currentUser")!!

    context.future(scope.future {
        val bookmarks = playQueue.getBookmarkMapByUsername(user)
        if (bookmarks.containsKey(id)) {
            playQueue.removeBookmarkByUsername(user, id)
            success(context, null)
            return@future
        }
        reject(context, Reject.NODATA)
    })
}