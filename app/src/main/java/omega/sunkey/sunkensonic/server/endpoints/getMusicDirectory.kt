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
        val songPaths = dao.getAllSongPaths()
        val musicfolder = getPathsWithIDs(songPaths)
        val path = musicfolder[id]
        if(path == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val allSongs = dao.getSongsByPath("%${path.name}%")
        if(allSongs.isEmpty()) {
            Log.i("getMusicDirectory", path.toString())
            reject(context, Reject.NODATA)
            return@future
        }
        //val songs = mutableListOf<SongWithMetadata>()
        /*val subdirs = mutableSetOf<String>()
        for (metasong in allSongs) {
            val relPath = metasong.song.path.removePrefix(path.name).removePrefix("/")
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
                    path.id.toString(),
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
        }*/
        val children = allSongs.map { it.toSongData() }
        val dir = Directory(
            id.toString(),
            path.name,
            children
        )
        success(context, Directories(dir))
    })
}