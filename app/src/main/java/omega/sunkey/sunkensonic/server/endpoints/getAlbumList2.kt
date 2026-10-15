package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumListView2
import omega.sunkey.sunkensonic.server.responses.AlbumListViewNew
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getAlbumList2(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    //val type = context.queryParam("type") // lets ignore for now
    context.future(scope.future {
        val allAlbums = dao.getAllAlbumsWithMeta()
        val albumList = mutableListOf<AlbumData>()
        allAlbums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            albumList.add(
                AlbumData(
                    id = it.album.id,
                    parent = it.album.artistId,
                    album = it.album.title,
                    coverArt = it.album.coverArt,
                    songCount = it.songs.size,
                    created = it.album.dateAdded.toIsoTime(),
                    duration = totalDuration,
                    playCount = it.album.playCount,
                    played = it.album.lastPlayed?.toIsoTime(),
                    artistId = it.artist.id,
                    artist = it.artist.name,
                    year = it.album.year ?: 2000,
                    userRating = it.album.userRating ?: 0,
                    starred = it.album.starred?.toIsoTime()
                )
            )
        }
        success(context, AlbumListViewNew(AlbumListView2(albumList)))
    })
}
