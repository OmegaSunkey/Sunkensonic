package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.SinglePlaylistData
import omega.sunkey.sunkensonic.server.responses.SinglePlaylistView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.md5
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.Playlist
import omega.sunkey.sunkensonic.server.room.PlaylistSong
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun createPlaylist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val name = context.queryParam("name")
    val playlistId = context.queryParam("playlistId")
    if (name == null && playlistId == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    val songIds = context.queryParams("songId")
    context.future(scope.future {
        if(playlistId != null && songIds.isNotEmpty()) {
            val savedPlaylist = dao.getPlaylist(playlistId)
            if (savedPlaylist == null) {
                reject(context, Reject.NODATA)
                return@future
            }
            val songDataList = mutableListOf<SongData>()
            savedPlaylist.songs?.forEach {
                songDataList.add(
                    SongData(
                        id = it.song.id,
                        parent = it.song.albumId,
                        title = it.song.title,
                        album = it.album.title,
                        artist = it.artist.name,
                        track = it.song.track,
                        year = it.song.year,
                        genre = it.song.genre,
                        coverArt = it.song.coverArt,
                        size = it.song.size,
                        contentType = conType(it.song.suffix),
                        suffix = it.song.suffix,
                        duration = it.song.duration,
                        bitRate = it.song.bitrate,
                        path = it.song.path,
                        created = it.song.dateAdded.toIsoTime(),
                        albumId = it.song.albumId,
                        artistId = it.song.artistId
                    )
                )
            }
            val (songList, songPlaylist, totalDuration) = getSongList(songIds, dao, savedPlaylist.playlist.name, savedPlaylist.songs?.size)
            dao.insertPlaylistSongs(songPlaylist)
            success(
                context,
                SinglePlaylistView(
                    SinglePlaylistData(
                        savedPlaylist.playlist.id,
                        savedPlaylist.playlist.name,
                        savedPlaylist.playlist.comment,
                        savedPlaylist.playlist.owner,
                        savedPlaylist.playlist.public,
                        savedPlaylist.songs?.size?.plus(songList.size) ?: songList.size,
                        savedPlaylist.playlist.duration + totalDuration,
                        savedPlaylist.playlist.created?.toIsoTime(),
                        Clock.System.now().epochSeconds.toIsoTime(),
                        songDataList + songList,
                    )
                )
            )
        }
        if (name != null && songIds.isNotEmpty()) {
            val (songList, songPlaylist, totalDuration) = getSongList(songIds, dao, name, 0)
            dao.insertPlaylist(Playlist(name.md5(), name, "", context.attribute<String>("currentUser")!!, false,songIds.size, totalDuration, Clock.System.now().epochSeconds, null))
            dao.insertPlaylistSongs(songPlaylist)
            success(
                context,
                SinglePlaylistView(
                    SinglePlaylistData(
                        name.md5(),
                        name,
                        "",
                        context.attribute<String>("currentUser")!!,
                        false,
                        songList.size,
                        totalDuration,
                        Clock.System.now().epochSeconds.toIsoTime(),
                        null,
                        songList
                    )
                )
            )
        } else if (name != null) {
            dao.insertPlaylist(Playlist(name.md5(), name, "", context.attribute<String>("currentUser")!!, false,0, 0, Clock.System.now().epochSeconds, null))
            success(
                context,
                SinglePlaylistView(
                    SinglePlaylistData(
                        name.md5(),
                        name,
                        "",
                        context.attribute<String>("currentUser")!!,
                        false,
                        0,
                        0,
                        Clock.System.now().epochSeconds.toIsoTime(),
                        null,
                        null
                    )
                )
            )
        } else {
            reject(context, Reject.MISSINGPARAM)
        }
    })
}

suspend fun getSongList(songIds: List<String>, dao: SubsonicDao, pname: String, songIndex: Int?): Triple<MutableList<SongData>, MutableList<PlaylistSong>, Int> {
    val songList = mutableListOf<SongData>()
    val songPlaylist = mutableListOf<PlaylistSong>()
    var index = songIndex ?: 0
    var totalDuration = 0
    songIds.forEach {
        val msong = dao.getSongWithMeta(it)
        if (msong != null) {
            totalDuration += msong.song.duration
            index += 1
            songList.add(
                SongData(
                    id = msong.song.id,
                    parent = msong.song.albumId,
                    title = msong.song.title,
                    album = msong.album.title,
                    artist = msong.artist.name,
                    track = msong.song.track,
                    year = msong.song.year,
                    genre = msong.song.genre,
                    coverArt = msong.song.coverArt,
                    size = msong.song.size,
                    contentType = conType(msong.song.suffix),
                    suffix = msong.song.suffix,
                    duration = msong.song.duration,
                    bitRate = msong.song.bitrate,
                    path = msong.song.path,
                    created = msong.song.dateAdded.toIsoTime(),
                    albumId = msong.song.albumId,
                    artistId = msong.song.artistId
                )
            )
            songPlaylist.add(PlaylistSong(pname.md5(), msong.song.id, index))
        }
    }
    return Triple(songList, songPlaylist, totalDuration)
}