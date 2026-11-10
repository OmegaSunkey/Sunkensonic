package omega.sunkey.sunkensonic.server.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AlbumInfoView(
    val albumInfo: AlbumInfo
)

data class AlbumInfo(
    val musicBrainzId: String,
    val smallImageUrl: String,
    val mediumImageUrl: String,
    val largeImageUrl: String
)

// JSON Objects
@Serializable
data class LastFmAlbumInfo(
    val album: LastFmAlbum
)

@Serializable
data class LastFmAlbum(
    val artist: String,
    val mbid: String,
    val tags: String,
    val name: String,
    val image: List<LastFmImages>,
    val tracks: LastFmTracks,
    val listeners: String,
    val playcount: String,
    val url: String
)

@Serializable
data class LastFmImages(
    val size: String,
    @SerialName("#text")
    val text: String
)

@Serializable
data class LastFmTracks(
    val track: List<LastFmTrack>
)

@Serializable
data class LastFmTrack(
    val streamable: LastFmStreamable,
    val duration: Int,
    val url: String,
    val name: String,
    @SerialName("@attr")
    val attr: LastFmAttr,
    val artist: LastFmArtistInAlbum
)

@Serializable
data class LastFmStreamable(
    val fulltrack: String,
    @SerialName("#text")
    val text: String
)

@Serializable
data class LastFmAttr(
    val rank: Int
)

@Serializable
data class LastFmArtistInAlbum(
    val url: String,
    val name: String,
    val mbid: String
)
