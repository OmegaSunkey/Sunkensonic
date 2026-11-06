package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumListView2
import omega.sunkey.sunkensonic.server.responses.AlbumListViewNew
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getAlbumList2(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val type = context.queryParam("type")
    val size = context.queryParam("size")?.toIntOrNull()
    val offset = context.queryParam("offset")?.toIntOrNull()
    //val fromYear = context.queryParam("fromYear")?.toIntOrNull()
    //val toYear = context.queryParam("toYear")?.toIntOrNull()
    val genre = context.queryParam("genre")
    val musicFolderId = context.queryParam("musicFolderId")
    context.future(scope.future {
        val allAlbums = when(type) {
            "newest" -> dao.getAllAlbumsByNewest(size ?: 500, offset ?: 0)
            "highest" -> dao.getAllAlbumsByRating(size ?: 500, offset ?: 0)
            "frequent" -> dao.getAllAlbumsByFrequency(size ?: 500, offset ?: 0)
            "recent" -> dao.getAllAlbumsByLastPlayed(size ?: 500, offset ?: 0)
            "starred" -> dao.getAllAlbumsByStarred(size ?: 500, offset ?: 0)
            /*"byYear" -> {
                if(fromYear != null && toYear != null) {
                    if(fromYear > toYear) {
                        dao.getAllAlbumsByYearDesc(size ?: 500, offset ?: 0, fromYear, toYear)
                    } else dao.getAllAlbumsByYearAsc(size ?: 500, offset ?: 0, fromYear, toYear)
                } else dao.getAllAlbumsByTitle(size ?: 500, offset ?: 0)
            }*/
            else -> dao.getAllAlbumsByTitle(500, 0)
        }
        val finalSize = if(size != null && size > allAlbums.size) allAlbums.size else size
        val albumList = mutableListOf<AlbumData>()
        allAlbums.forEach {
            var totalDuration = 0
            it.songs.forEach { song ->
                totalDuration += song.duration
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
            "random" -> albumList.apply {
                retainAll(albumList.drop(offset ?: 0).subList(0, finalSize ?: albumList.size))
                shuffle()
            }
            "alphabeticalByName" -> albumList.apply {
                retainAll(albumList.drop(offset ?: 0).subList(0, finalSize ?: albumList.size))
                sortBy { it.name }
            }
            "alphabeticalByArtist" -> albumList.apply {
                retainAll(albumList.drop(offset ?: 0).subList(0, finalSize ?: albumList.size))
                sortBy { it.artist }
            }
            "starred" -> albumList.apply {
                retainAll(albumList.drop(offset ?: 0).subList(0, finalSize ?: albumList.size))
                removeIf { it.starred == null }
            }
            "byGenre" -> {
                if (genre != null) albumList.apply {
                    retainAll(albumList.drop(offset ?: 0).subList(0, finalSize ?: albumList.size))
                    removeIf { !it.genre.contentEquals(genre) }
                    sortBy { it.name }
                }
            }
        }
        success(context, AlbumListViewNew(AlbumListView2(albumList)))
    })
}
