package omega.sunkey.sunkensonic.server.responses

import omega.sunkey.sunkensonic.server.room.AlbumsWithMeta
import omega.sunkey.sunkensonic.server.room.ArtistsWithMeta
import omega.sunkey.sunkensonic.server.room.SongWithMetadata
import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun AlbumsWithMeta.toAlbumDataWithMeta(): AlbumData {
    val songList = mutableListOf<SongData>()
    var totalDuration = 0
    this.songs.forEach {
        totalDuration += it.duration
        songList.add(SongData(
            id = it.id,
            parent = it.albumId,
            title = it.title,
            album = this.album.title,
            artist = this.artist.name,
            track = it.track,
            discNumber = it.discNumber,
            year = it.year,
            genre = it.genre,
            coverArt = it.coverArt,
            size = it.size,
            contentType = conType(it.suffix),
            suffix = it.suffix,
            duration = it.duration,
            bitRate = it.bitrate,
            path = it.path,
            created = it.dateAdded.toIsoTime(),
            albumId = it.albumId,
            artistId = it.artistId
        ))
    }
    
    return AlbumData(
        id = this.album.id,
        parent = this.album.artistId,
        album = this.album.title,
        coverArt = this.album.coverArt,
        songCount = this.songs.size,
        created = this.album.dateAdded.toIsoTime(),
        duration = totalDuration,
        playCount = this.album.playCount,
        played = this.album.lastPlayed?.toIsoTime(),
        artistId = this.album.artistId,
        artist = this.artist.name,
        year = this.album.year,
        genre = this.songs[0].genre ?: "Unknown Genre",
        song = songList,
        userRating = this.album.userRating ?: 0,
        starred = this.album.starred?.toIsoTime()
    )
}

fun AlbumsWithMeta.toAlbumData(duration: Int): AlbumData {
    return AlbumData(
        id = this.album.id,
        parent = this.album.artistId,
        album = this.album.title,
        coverArt = this.album.coverArt,
        songCount = this.songs.size,
        created = this.album.dateAdded.toIsoTime(),
        duration = duration,
        playCount = this.album.playCount,
        played = this.album.lastPlayed?.toIsoTime(),
        artistId = this.artist.id,
        artist = this.artist.name,
        year = this.album.year ?: 2000,
        userRating = this.album.userRating ?: 0,
        starred = this.album.starred?.toIsoTime()
    )
}

fun ArtistsWithMeta.toArtistDataWithMeta(): ArtistData {
    val albums = mutableListOf<AlbumData>()
    this.albums.forEach {
        var totalDuration = 0
        it.songs.forEach { s ->
            totalDuration += s.duration
        }
        albums.add(AlbumData(
            id = it.album.id,
            parent = it.album.artistId,
            album = it.album.title,
            title = it.album.title,
            name = it.album.title,
            coverArt = it.album.coverArt,
            songCount = it.songs.size,
            created = it.album.dateAdded.toIsoTime(),
            duration = totalDuration,
            playCount = it.album.playCount,
            played = it.album.lastPlayed?.toIsoTime(),
            artistId = this.artist.id,
            artist = this.artist.name,
            year = it.album.year ?: 2000,
            userRating = it.album.userRating ?: 0,
            starred = it.album.starred?.toIsoTime()
        ))
    }
    
    return ArtistData(
        id = this.artist.id,
        name = this.artist.name,
        albumCount = this.albums.size,
        coverArt = this.albums[0].album.coverArt,
        userRating = this.artist.userRating ?: 0,
        playCount = this.artist.playCount,
        played = this.artist.lastPlayed?.toIsoTime(),
        starred = this.artist.starred?.toIsoTime(),
        album = albums
    )
}

fun ArtistsWithMeta.toArtistData(): ArtistData {
    return ArtistData(
        id = this.artist.id,
        name = this.artist.name,
        albumCount = this.albums.size,
        coverArt = this.albums[0].album.coverArt,
        userRating = this.artist.userRating ?: 0,
        playCount = this.artist.playCount,
        played = this.artist.lastPlayed?.toIsoTime(),
        starred = this.artist.starred?.toIsoTime(),
    )
}

fun SongWithMetadata.toSongData(): SongData {
    return SongData(
        id = this.song.id,
        parent = this.album.id,
        isDir = false,
        title = this.song.title,
        album = this.album.title,
        artist = this.artist.name,
        track = this.song.track,
        year = this.song.year,
        genre = this.song.genre,
        coverArt = this.song.coverArt,
        size = this.song.size,
        contentType = conType(this.song.suffix!!),
        suffix = this.song.suffix,
        userRating = this.song.userRating ?: 0,
        starred = this.song.starred?.toIsoTime(),
        playCount = this.song.playCount,
        played = this.song.lastPlayed?.toIsoTime(),
        duration = this.song.duration,
        bitRate = this.song.bitrate,
        path = this.song.path,
        discNumber = this.song.discNumber,
        created = this.song.dateAdded.toIsoTime(),
        albumId = this.song.albumId,
        artistId = this.song.artistId
    )
}

@OptIn(ExperimentalTime::class)
fun SongWithMetadata.toNowPlayingData(username: String, startedAt: Long): NowPlayingData {
    return NowPlayingData(
        this.song.id,
        this.album.id,
        false,
        this.song.title,
        this.album.title,
        this.artist.name,
        this.song.track,
        this.song.year,
        this.song.genre,
        this.song.coverArt,
        this.song.size,
        conType(this.song.suffix!!),
        this.song.suffix,
        this.song.userRating ?: 0,
        this.song.starred?.toIsoTime(),
        this.song.duration,
        this.song.bitrate,
        this.song.path,
        this.song.discNumber,
        this.song.dateAdded.toIsoTime(),
        this.song.albumId,
        this.song.artistId,
        username = username,
        minutesAgo = ((Clock.System.now().toEpochMilliseconds() - startedAt) / 60000).toInt(),
        playerId = 0
    )
}