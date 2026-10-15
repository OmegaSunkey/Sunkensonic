package omega.sunkey.sunkensonic.server.responses

data class Playlists(
    val playlist: List<PlaylistData>
)

data class PlaylistsView(
    val playlists: Playlists
)

data class PlaylistData(
    val id: String,
    val name: String,
    val comment: String,
    val owner: String,
    val public: Boolean,
    val songCount: Int,
    val duration: Int,
    val created: String?,
    val changed: String?
)

data class SinglePlaylistView(
    val playlist: SinglePlaylistData
)

data class SinglePlaylistData(
    val id: String,
    val name: String,
    val comment: String,
    val owner: String,
    val public: Boolean,
    val songCount: Int,
    val duration: Int,
    val created: String?,
    val changed: String?,
    val entry: List<SongData>?
)