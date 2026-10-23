package omega.sunkey.sunkensonic.server.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Index
import androidx.room.Junction

@Entity(
    tableName = "songs",
    foreignKeys = [ForeignKey(
        entity = Album::class,
        parentColumns = ["id"],
        childColumns = ["album_id"],
        onDelete = CASCADE
    )],
    indices = [Index("album_id")]
)
data class Song(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "track") val track: Int? = null,
    @ColumnInfo(name = "disc_number") val discNumber: Int? = null,
    @ColumnInfo(name = "year") val year: Int? = null,
    @ColumnInfo(name = "genre") val genre: String? = null,
    @ColumnInfo(name = "size") val size: Long = 0,
    @ColumnInfo(name = "suffix") val suffix: String? = "",
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "bitrate") val bitrate: Int? = null,
    @ColumnInfo(name = "date_added") val dateAdded: Long = 0,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "album_id") val albumId: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "cover_uri") val coverArt: String,
    @ColumnInfo(name = "starred") val starred: Long? = null,
    @ColumnInfo(name = "user_rating") val userRating: Int? = null,
    @ColumnInfo(name = "play_count") val playCount: Int = 0,
    @ColumnInfo(name = "last_played") val lastPlayed: Long? = null,
    @ColumnInfo(name = "folder_id") val folderId: Int = 0
)

@Entity(
    tableName = "albums",
    foreignKeys = [ForeignKey(
        entity = Artist::class,
        parentColumns = ["id"],
        childColumns = ["artist_id"],
        onDelete = CASCADE
    )],
    indices = [Index("artist_id")]
)
data class Album(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "year") val year: Int? = null,
    @ColumnInfo(name = "cover_uri") val coverArt: String,
    @ColumnInfo(name = "date_added") val dateAdded: Long = 0,
    @ColumnInfo(name = "starred") val starred: Long? = null,
    @ColumnInfo(name = "user_rating") val userRating: Int? = null,
    @ColumnInfo(name = "play_count") val playCount: Int = 0,
    @ColumnInfo(name = "last_played") val lastPlayed: Long? = null,
    @ColumnInfo(name = "folder_id") val folderId: Int? = null
)

@Entity(
    tableName = "artists"
)
data class Artist(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "starred") val starred: Long? = null,
    @ColumnInfo(name = "user_rating") val userRating: Int? = null,
    @ColumnInfo(name = "play_count") val playCount: Int = 0,
    @ColumnInfo(name = "last_played") val lastPlayed: Long? = null,
    @ColumnInfo(name = "folder_id") val folderId: Int? = null
)

data class AlbumsWithMeta(
    @Embedded val album: Album,

    @Relation(
        parentColumn = "id",
        entityColumn = "album_id"
    )
    val songs: List<Song>,

    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: Artist
)

data class ArtistsWithMeta(
    @Embedded val artist: Artist,

    @Relation(
        entity = Album::class,
        parentColumn = "id",
        entityColumn = "artist_id"
    )
    val albums: List<AlbumsWithMeta>
)

data class SongWithMetadata(
    @Embedded val song: Song,

    @Relation(
        parentColumn = "album_id",
        entityColumn = "id"
    )
    val album: Album,
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: Artist
)

@Entity(
    tableName = "playlists"
)
data class Playlist(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "comment") val comment: String = "",
    @ColumnInfo(name = "owner") val owner: String,
    @ColumnInfo(name = "public") val public: Boolean = false,
    @ColumnInfo(name = "song_count") val songCount: Int,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "created") val created: Long? = null,
    @ColumnInfo(name = "changed") val changed: Long? = null
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlist_id", "song_id"]
)
data class PlaylistSong(
    @ColumnInfo(name = "playlist_id") val playlistId: String,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int
)

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,

    @Relation(
        entity = Song::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSong::class,
            parentColumn = "playlist_id",
            entityColumn = "song_id"
        )
    )
    val songs: List<SongWithMetadata>?
)

@Entity(
    tableName = "states"
)
data class States(
    @PrimaryKey val id: String,
    val type: String,
    val starred: Long? = null,
    val rating: Int? = null,
    val playCount: Int = 0,
    val lastPlayed: Long? = null
)

@Entity(
    tableName = "scrobbles"
)
data class Scrobble(
    @PrimaryKey val id: String,
    val username: String,
    val startedAt: Long,
    val expiresAt: Long
)