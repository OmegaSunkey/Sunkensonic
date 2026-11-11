package omega.sunkey.sunkensonic.server.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AlbumInfoView(
    val albumInfo: AlbumInfo
)

data class AlbumInfo(
    val notes: String,
    val musicBrainzId: String,
    val smallImageUrl: String,
    val mediumImageUrl: String,
    val largeImageUrl: String
)

data class ArtistInfoView(
    val artistInfo: ArtistInfo
)

data class ArtistInfoViewTheSequel(
    val artistInfo2: ArtistInfo
)

data class ArtistInfo(
    val biography: String,
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
    val url: String,
    val wiki: LastFmWiki = LastFmWiki("Who knows", "There is no wiki", "There is no information about this album")
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

@Serializable
data class LastFmWiki(
    val published: String,
    val summary: String,
    val content: String
)

@Serializable
data class LastFmArtistInfo(
    val artist: LastFmArtist
)

@Serializable
data class LastFmArtist(
    val name: String,
    val mbid: String,
    val url: String,
    val image: List<LastFmImages>,
    val streamable: String,
    val ontour: String,
    val stats: LastFmStats,
    val similar: LastFmSimilar,
    val tags: LastFmTags,
    val bio: LastFmBio
)

@Serializable
data class LastFmStats(
    val listeners: String,
    val playcount: String
)

@Serializable
data class LastFmSimilar(
    val artist: List<LastFmSimilarArtist>
)

@Serializable
data class LastFmSimilarArtist(
    val name: String,
    val url: String,
    val image: List<LastFmImages>
)

@Serializable
data class LastFmTags(
    val tag: List<LastFmTag>
)

@Serializable
data class LastFmTag(
    val name: String,
    val url: String
)

@Serializable
data class LastFmBio(
    val links: LastFmLinks,
    val published: String,
    val summary: String,
    val content: String
)

@Serializable
data class LastFmLinks(
    val link: LastFmLink
)

@Serializable
data class LastFmLink(
    @SerialName("#text")
    val text: String,
    val rel: String,
    val href: String
)