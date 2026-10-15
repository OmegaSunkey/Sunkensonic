package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.Directories
import omega.sunkey.sunkensonic.server.responses.Directory
import omega.sunkey.sunkensonic.server.responses.DirectoryChild
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SongWithMetadata
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
import java.net.URLDecoder
import java.net.URLEncoder

fun getMusicDirectory(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val path = URLDecoder.decode(id, "UTF-8")
        val allSongs = dao.getSongsByPath(path)
        if(allSongs.isEmpty()) {
            reject(context, Reject.NODATA)
            return@future
        }
        val songs = mutableListOf<SongWithMetadata>()
        val subdirs = mutableSetOf<String>()
        for (metasong in allSongs) {
            val relPath = metasong.song.path.removePrefix(path).removePrefix("/")
            if(relPath.contains("/")) {
                val whichsubdir = relPath.substringBefore("/")
                subdirs.add(whichsubdir)
            } else {
                songs.add(metasong)
            }
        }
        val children = mutableListOf<DirectoryChild>()
        for(subdir in subdirs) {
            val subdirPath = "$path/$subdir"
            children.add(
                DirectoryChild(
                    URLEncoder.encode(subdirPath, "UTF-8"),
                    name = subdir,
                    isDir = true
                )
            )
        }
        for(metasong in songs) {
            children.add(
                DirectoryChild(
                    metasong.song.id,
                    URLEncoder.encode(path, "UTF-8"),
                    metasong.song.title,
                    null,
                    metasong.album.title,
                    metasong.artist.name,
                    false,
                    metasong.song.duration,
                    null,
                    metasong.song.size,
                    metasong.song.suffix,
                    conType(metasong.song.suffix!!),
                    metasong.song.path,
                    metasong.song.dateAdded.toIsoTime(),
                    metasong.song.coverArt
                )
            )
        }
        val dir = Directory(
            id,
            path.substringAfterLast("/"),
            children
        )
        success(context, Directories(dir))
    })
}