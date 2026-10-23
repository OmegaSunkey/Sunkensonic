package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.MusicFolder
import omega.sunkey.sunkensonic.server.responses.MusicFolders
import omega.sunkey.sunkensonic.server.responses.MusicFoldersView
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getMusicFolders(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allpaths = dao.getAllSongPaths()
        val paths = mutableSetOf<MusicFolder>()
        allpaths.forEach {
            paths.add(
                MusicFolder(
                    it.folderId,
                    it.path.substringBeforeLast("/").substringAfterLast("/")
                )
            )
        }
        success(context, MusicFoldersView(MusicFolders(paths.toList())))
    })
}