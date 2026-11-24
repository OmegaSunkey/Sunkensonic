package omega.sunkey.sunkensonic.server.responses

data class Bookmark(
    val entry: SongData,
    val position: Long,
    val username: String,
    val comment: String,
    val created: String,
    val changed: String? = null,
)

data class BookmarksView(
    val bookmarks: Bookmarks
)

data class Bookmarks(
    val bookmark: List<Bookmark>
)
