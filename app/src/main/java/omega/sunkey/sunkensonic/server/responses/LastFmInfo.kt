package omega.sunkey.sunkensonic.server.responses

data class AlbumInfoView(
    val albumInfo: AlbumInfo
)

data class AlbumInfo(
    val notes: String,
    val musicBrainzId: String?,
    val smallImageUrl: String?,
    val mediumImageUrl: String?,
    val largeImageUrl: String?
)

data class ArtistInfoView(
    val artistInfo: ArtistInfo
)

data class ArtistInfoViewTheSequel(
    val artistInfo2: ArtistInfo
)

data class ArtistInfo(
    val biography: String,
    val musicBrainzId: String?,
    val smallImageUrl: String?,
    val mediumImageUrl: String?,
    val largeImageUrl: String?
)