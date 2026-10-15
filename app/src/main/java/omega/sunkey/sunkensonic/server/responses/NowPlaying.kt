package omega.sunkey.sunkensonic.server.responses

data class NowPlaying(
    val entry: List<NowPlayingData>
)

data class NowPlayingView(
    val nowPlaying: NowPlaying
)

data class NowPlayingData(
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
    val duration: Int? = null,
    val bitRate: Int? = null,
    val path: String? = null,
    val discNumber: Int? = null,
    val created: String? = "2021-07-22T02:09:31+00:00",
    val albumId: String? = null,
    val artistId: String? = null,
    val type: String = "music",
    val isVideo: Boolean = false,
    val username: String,
    val minutesAgo: Int,
    val playerId: Int
)
