package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.ListView
import omega.sunkey.sunkensonic.server.responses.MusicFolder
import omega.sunkey.sunkensonic.server.responses.User
import omega.sunkey.sunkensonic.server.responses.UsersView
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getUsers(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allpaths = dao.getAllSongPaths()
        val ids = mutableListOf<Int>()
        allpaths.forEach {
            ids.add(it.folderId)
        }
        val hardcodedUser = User( //im just too lazy now
            "sunkey",
            "",
            folder = ids
        )
        success(context, UsersView(ListView(listOf(hardcodedUser))))
    })
}