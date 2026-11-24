package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.SearchView2
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun search2(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val query = context.queryParam("query")
    val musicFolderId = context.queryParam("musicFolderId")?.toIntOrNull()
    val artistCount = context.queryParam("artistCount")?.toIntOrNull() ?: 20
    val albumCount = context.queryParam("albumCount")?.toIntOrNull() ?: 20
    val songCount = context.queryParam("songCount")?.toIntOrNull() ?: 20

    val artistOffset = context.queryParam("artistOffset")?.toIntOrNull() ?: 0
    val albumOffset = context.queryParam("albumOffset")?.toIntOrNull() ?: 0
    val songOffset = context.queryParam("songOffset")?.toIntOrNull() ?: 0

    context.future(scope.future {
        success(context, SearchView2(search(dao, query, musicFolderId, artistCount, albumCount, songCount, artistOffset, albumOffset, songOffset)))
    })
}