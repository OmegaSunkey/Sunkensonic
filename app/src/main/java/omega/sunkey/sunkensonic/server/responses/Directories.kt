package omega.sunkey.sunkensonic.server.responses

data class Directory(
    val id: String,
    val name: String,
    //val child: List<DirectoryChild>
    val child: List<SongData>
)

/*data class DirectoryChild(
    val id: String,
    val parent: String? = null,
    val title: String? = null,
    val name: String? = null,
    val album: String? = null,
    val artist: String? = null,
    val isDir: Boolean,
    val duration: Int? = null,
    val bitRate: Int? = null,
    val size: Long? = null,
    val suffix: String? = null,
    val contentType: String? = null,
    val path: String? = null,
    val created: String? = "2021-07-22T02:09:31+00:00",
    val coverArt: String = ""
)*/

data class Directories(val directory: Directory)
