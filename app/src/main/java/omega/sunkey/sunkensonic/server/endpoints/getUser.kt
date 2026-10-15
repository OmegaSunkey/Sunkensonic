package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.MusicFolder
import omega.sunkey.sunkensonic.server.responses.User
import omega.sunkey.sunkensonic.server.responses.UserView
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getUser(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val allpaths = dao.getAllSongPaths()
        val dcpaths = mutableMapOf<Int, MusicFolder>()
        for (path in allpaths) {
            val key = path.substringBeforeLast("/").substringAfterLast("/").hashCode()
            if(!dcpaths.containsKey(key)) {
                dcpaths[key] = MusicFolder(key, path.substringBeforeLast("/").substringAfterLast("/"))
            }
        }
        val ids = dcpaths.map { (key, _) ->
            key
        }
        val hardcodedUser = User( //im just too lazy now
            "sunkey",
            "",
            folder = ids
        )
        success(context, UserView(hardcodedUser))
    })
}