package omega.sunkey.sunkensonic.server.responses

data class UserView(
    val user: User
)

data class UsersView(
    val users: ListView
)

data class ListView(
    val user: List<User>
)

data class User(
    val username: String,
    val email: String,
    val scrobblingEnabled: Boolean = true,
    val adminRole: Boolean = true,
    val settingsRole: Boolean = true,
    val downloadRole: Boolean = true,
    val uploadRole: Boolean = true,
    val playlistRole: Boolean = true,
    val coverArtRole: Boolean = true,
    val commentRole: Boolean = true,
    val podcastRole: Boolean = true,
    val streamRole: Boolean = true,
    val jukeboxRole: Boolean = true,
    val shareRole: Boolean = true,
    val folder: List<Int>
)