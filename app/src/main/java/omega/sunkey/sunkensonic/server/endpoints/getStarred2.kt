package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.StarredData
import omega.sunkey.sunkensonic.server.responses.Starreds
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.responses.toArtistData
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getStarred2(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allStars = dao.getAllStarred()
        val songList = mutableListOf<SongData>()
        val albumList = mutableListOf<AlbumData>()
        val artistList = mutableListOf<ArtistData>()
        allStars.forEach { starred ->
            when(starred.type) {
                "song" -> {
                    val it = dao.getSongWithMeta(starred.id) ?: return@forEach
                    songList.add(
                        it.toSongData()
                    )
                }
                "album" -> {
                    val it = dao.getAlbumWithSongs(starred.id) ?: return@forEach
                    var totalDuration = 0
                    it.songs.forEach { song ->
                        totalDuration += song.duration
                    }
                    albumList.add(
                        it.toAlbumData(totalDuration)
                    )
                }
                "artist" -> {
                    val it = dao.getArtistWithDetails(starred.id) ?: return@forEach
                    artistList.add(
                        it.toArtistData()
                    )
                }
            }
        }
        success(context, Starreds(StarredData(artistList, albumList, songList)))
    })
}