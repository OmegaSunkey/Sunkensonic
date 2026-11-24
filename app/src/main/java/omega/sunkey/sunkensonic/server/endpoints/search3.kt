package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.Search
import omega.sunkey.sunkensonic.server.responses.SearchView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.responses.toArtistData
import omega.sunkey.sunkensonic.server.responses.toSongData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun search3(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val query = context.queryParam("query")
    val musicFolderId = context.queryParam("musicFolderId")?.toIntOrNull()
    val artistCount = context.queryParam("artistCount")?.toIntOrNull() ?: 20
    val albumCount = context.queryParam("albumCount")?.toIntOrNull() ?: 20
    val songCount = context.queryParam("songCount")?.toIntOrNull() ?: 20

    val artistOffset = context.queryParam("artistOffset")?.toIntOrNull() ?: 0
    val albumOffset = context.queryParam("albumOffset")?.toIntOrNull() ?: 0
    val songOffset = context.queryParam("songOffset")?.toIntOrNull() ?: 0

    context.future(scope.future {
        success(context, SearchView(search(dao, query, musicFolderId, artistCount, albumCount, songCount, artistOffset, albumOffset, songOffset)))
    })
}

suspend fun search(dao: SubsonicDao, query: String?, musicFolderId: Int?, artistCount: Int, albumCount: Int, songCount: Int, artistOffset: Int, albumOffset: Int, songOffset: Int): Search {
    if (query == null) {
        val artists = dao.getAllArtistsWithMeta().map { it.toArtistData() }
        val albums = dao.getAllAlbumsByTitle(1000, 0).map {
            var totalDuration = 0
            it.songs.forEach { song ->
                totalDuration += song.duration
            }
            it.toAlbumData(totalDuration)
        }
        val songs = dao.getAllSongsWithMeta().map { it.toSongData() }
        return Search(artists, albums, songs)
    }

    val artists = dao.searchArtists(query, artistCount, artistOffset)
    val albums = dao.searchAlbums(query, albumCount, albumOffset)
    val songs = dao.searchSongs(query, songCount, songOffset)
    val metaartist = mutableListOf<ArtistData>()
    val metaalbum = mutableListOf<AlbumData>()
    val metasong = mutableListOf<SongData>()
    artists.forEach {
        if(musicFolderId != null) {
            if (it.artist.folderId == musicFolderId) metaartist.add(
                it.toArtistData()
            )
        } else metaartist.add(it.toArtistData())
    }
    albums.forEach {
        var totalDuration = 0
        it.songs.forEach { s ->
            totalDuration += s.duration
        }
        if (musicFolderId != null) {
            if (it.album.folderId == musicFolderId) metaalbum.add(
                it.toAlbumData(totalDuration)
            )
        } else metaalbum.add(it.toAlbumData(totalDuration))
    }
    songs.forEach {
        if (musicFolderId != null) {
            if (it.song.folderId == musicFolderId) metasong.add(
                it.toSongData()
            )
        } else metasong.add(it.toSongData())
    }
    return Search(metaartist, metaalbum, metasong)
}