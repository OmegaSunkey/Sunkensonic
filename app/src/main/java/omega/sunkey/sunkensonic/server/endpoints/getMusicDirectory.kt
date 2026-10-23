package omega.sunkey.sunkensonic.server.endpoints

import android.util.Log
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.Directories
import omega.sunkey.sunkensonic.server.responses.Directory
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getMusicDirectory(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")?.toIntOrNull()
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        if (id == 312) {
            val allSongs = dao.getAllSongsWithMeta()
            val children = allSongs.map { it.toSongData() }
            success(context, Directories(Directory(id.toString(), "Global", children)))
            return@future
        }
        val songPath = dao.getAllSongPaths().find { it.folderId == id }
        if(songPath == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val allSongs = dao.getSongsByFolderId(songPath.folderId)
        if(allSongs.isEmpty()) {
            reject(context, Reject.NODATA)
            return@future
        }
        val children = allSongs.map { it.toSongData() }
        val dir = Directory(
            id.toString(),
            songPath.path.substringBeforeLast("/").substringAfterLast("/"),
            children
        )
        success(context, Directories(dir))
    })
}