package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.Bookmarks
import omega.sunkey.sunkensonic.server.responses.BookmarksView
import omega.sunkey.sunkensonic.server.responses.SubsonicPlayQueue
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getBookmarks(context: Context, scope: CoroutineScope, dao: SubsonicDao, playQueue: SubsonicPlayQueue) {
    context.future(scope.future {
        val bookmarks = playQueue.getBookmarksByUsername(context.attribute<String>("currentUser")!!)

        for (bookmark in bookmarks) {
            val song = dao.getSong(bookmark.entry.id)
            if (song == null) playQueue.removeBookmarkByUsername(context.attribute<String>("currentUser!!")!!, bookmark.entry.id)
        }

        success(context, BookmarksView(Bookmarks(bookmarks)))
    })
}