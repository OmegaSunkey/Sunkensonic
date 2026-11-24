package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.Bookmark
import omega.sunkey.sunkensonic.server.responses.SubsonicPlayQueue
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.time.Clock

fun createBookmark(context: Context, scope: CoroutineScope, dao: SubsonicDao, playQueue: SubsonicPlayQueue) {
    val id = context.queryParam("id")
    val position = context.queryParam("position")?.toLongOrNull()
    val comment = context.queryParam("comment")
    val user = context.attribute<String>("currentUser")!!

    if (id == null || position == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }

    context.future(scope.future {
        val bookmarks = playQueue.getBookmarkMapByUsername(user)
        if (bookmarks.containsKey(id)) {
            playQueue.updateBookmarkByUsername(
                user,
                id,
                bookmarks[id]!!.copy(
                    position = position,
                    comment = comment ?: "",
                    changed = Clock.System.now().epochSeconds.toIsoTime()
                )
            )
        }
        val song = dao.getSongWithMeta(id)?.toSongData()
        if (song != null) {
            val bookmark = Bookmark(
                entry = song,
                position = position,
                username = user,
                comment = comment ?: "",
                created = Clock.System.now().epochSeconds.toIsoTime()!!
            )
            playQueue.addBookmarkByUsername(user, song.id, bookmark)
            success(context, null)
            return@future
        }

        reject(context, Reject.NODATA)
    })
}