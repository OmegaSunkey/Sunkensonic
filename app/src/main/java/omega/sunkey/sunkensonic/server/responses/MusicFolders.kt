package omega.sunkey.sunkensonic.server.responses

data class MusicFoldersView(
    val musicFolders: MusicFolders
)

data class MusicFolders(
    val musicFolder: List<MusicFolder>
)

data class MusicFolder(
    val id: Int,
    val name: String
)