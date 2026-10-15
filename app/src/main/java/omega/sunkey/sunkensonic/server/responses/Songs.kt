package omega.sunkey.sunkensonic.server.responses

data class SongView(
    val song: SongData
)

data class SongData(
    val id: String,
    val parent: String,
    val isDir: Boolean = false,
    val title: String,
    val album: String? = null,
    val artist: String? = null,
    val track: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val coverArt: String? = null,
    val size: Long? = null,
    val contentType: String? = null,
    val suffix: String? = null,
    val userRating: Int = 0,
    val starred: String? = null,
    val playCount: Int = 0,
    val played: String? = null,
    val duration: Int? = null,
    val bitRate: Int? = null,
    val path: String? = null,
    val discNumber: Int? = null,
    val created: String? = "2021-07-22T02:09:31+00:00", //PLEASE FILL WITH AN ACTUAL DATE
    val albumId: String? = null,
    val artistId: String? = null,
    val type: String = "music",
    val isVideo: Boolean = false
)