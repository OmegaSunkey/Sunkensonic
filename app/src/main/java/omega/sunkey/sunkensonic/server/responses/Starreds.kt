package omega.sunkey.sunkensonic.server.responses

data class Starreds(
    val starred2: StarredData
)

data class StarredData(
    val artist: List<ArtistData>,
    val album: List<AlbumData>,
    val song: List<SongData>
)

data class Starreds1(
    val starred: StarredData
)
