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
        val dcpaths = getPathsWithIDs(allpaths)
        success(context, MusicFoldersView(MusicFolders(dcpaths.values.toList())))
    })
}

fun getPathsWithIDs(paths: List<String>): Map<Int, MusicFolder> {
    val musicpaths = mutableMapOf<Int, MusicFolder>()
    for (path in paths) {
        val key = path.substringBeforeLast("/").substringAfterLast("/").hashCode()
        if(!musicpaths.containsKey(key)) {
            musicpaths[key] = MusicFolder(key, path.substringBeforeLast("/").substringAfterLast("/"))
        }
    }
    return musicpaths
}