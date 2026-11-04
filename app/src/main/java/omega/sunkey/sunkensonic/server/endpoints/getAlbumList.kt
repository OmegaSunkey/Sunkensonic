package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumListView1
import omega.sunkey.sunkensonic.server.responses.AlbumListView2
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getAlbumList(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val type = context.queryParam("type")
    val size = context.queryParam("size")?.toIntOrNull()
    val offset = context.queryParam("offset")?.toIntOrNull()
    //val fromYear = context.queryParam("fromYear")?.toIntOrNull()
    //val toYear = context.queryParam("toYear")?.toIntOrNull()
    val genre = context.queryParam("genre")
    val musicFolderId = context.queryParam("musicFolderId")
    context.future(scope.future {
        val allAlbums = dao.getAllAlbumsWithMeta(size ?: 500, offset ?: 0)
        val albumList = mutableListOf<AlbumData>()
        allAlbums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            if(musicFolderId != null && musicFolderId != "312") {
                if(it.album.folderId == musicFolderId.toInt()) albumList.add(
                    it.toAlbumData(totalDuration)
                )
            } else {
                albumList.add(
                    it.toAlbumData(totalDuration)
                )
            }
        }
        when(type) {
            "random" -> albumList.shuffle()
            "newest" -> albumList.sortBy { it.created }
            "highest" -> albumList.sortBy { it.userRating }
            "frequent" -> albumList.sortBy { it.playCount }
            "recent" -> albumList.sortBy { it.played }
            "alphabeticalByName" -> albumList.sortBy { it.name }
            "alphabeticalByArtist" -> albumList.sortBy { it.artist }
            "starred" -> albumList.removeIf { it.starred == null }
            "byGenre" -> {
                if (genre != null) albumList.removeIf { it.genre != genre }
            }
        }
        success(context, AlbumListView1(AlbumListView2(albumList)))
    })
}
