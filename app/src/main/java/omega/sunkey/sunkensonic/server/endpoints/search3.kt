package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.Search
import omega.sunkey.sunkensonic.server.responses.SearchView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.math.min

fun search3(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val query = context.queryParam("query")
    if (query == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val artistCount = if(context.queryParam("artistCount") != null) min(context.queryParam("artistCount")!!.toInt(), 40) else 20
    val albumCount = if(context.queryParam("albumCount") != null) min(context.queryParam("albumCount")!!.toInt(), 40) else 20
    val songCount = if(context.queryParam("songCount") != null) min(context.queryParam("songCount")!!.toInt(), 40) else 20

    val artistOffset = context.queryParam("artistOffset")?.toIntOrNull() ?: 0
    val albumOffset = context.queryParam("albumOffset")?.toIntOrNull() ?: 0
    val songOffset = context.queryParam("songOffset")?.toIntOrNull() ?: 0

    context.future(scope.future {
        val artists = dao.searchArtists(query, artistCount, artistOffset)
        val albums = dao.searchAlbums(query, albumCount, albumOffset)
        val songs = dao.searchSongs(query, songCount, songOffset)
        val metaartist = mutableListOf<ArtistData>()
        val metaalbum = mutableListOf<AlbumData>()
        val metasong = mutableListOf<SongData>()
        artists.forEach {
            metaartist.add(
                ArtistData(
                    it.artist.id,
                    it.artist.name,
                    it.albums.size,
                    it.albums[0].album.coverArt,
                    it.artist.userRating ?: 0,
                    "",
                    it.artist.playCount,
                    it.artist.lastPlayed?.toIsoTime(),
                    it.artist.starred?.toIsoTime()
                )
            )
        }
        albums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            metaalbum.add(
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
        songs.forEach {
            metasong.add(
                SongData(
                    id = it.song.id,
                    parent = it.album.id,
                    isDir = false,
                    title = it.song.title,
                    album = it.album.title,
                    artist = it.artist.name,
                    track = it.song.track,
                    year = it.song.year,
                    genre = it.song.genre,
                    coverArt = it.song.coverArt,
                    size = it.song.size,
                    contentType = conType(it.song.suffix!!),
                    suffix = it.song.suffix,
                    duration = it.song.duration,
                    bitRate = it.song.bitrate,
                    path = it.song.path,
                    discNumber = it.song.discNumber,
                    created = it.song.dateAdded.toIsoTime(),
                    albumId = it.song.albumId,
                    artistId = it.song.artistId,
                    playCount = it.song.playCount,
                    played = it.song.lastPlayed?.toIsoTime()
                )
            )
        }
        success(context, SearchView(Search(metaartist, metaalbum, metasong)))
    })
}
