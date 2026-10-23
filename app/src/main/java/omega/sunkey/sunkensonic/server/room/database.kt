package omega.sunkey.sunkensonic.server.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Database(entities = [Artist::class, Album::class, Song::class, Playlist::class, PlaylistSong::class, States::class, Scrobble::class], version = 1)
abstract class SubsonicDatabase : RoomDatabase() {
    abstract fun subsonicDao(): SubsonicDao
    companion object {
        @Volatile
        private var INSTANCE: SubsonicDatabase? = null
        fun getInstance(context: Context): SubsonicDatabase {
            return INSTANCE?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    SubsonicDatabase::class.java,
                    "subsonic_database"
                )
                    .fallbackToDestructiveMigration() //DONT FORGET !!!!!!!!!
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}

@Dao
interface SubsonicDao {
    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsWithMeta(): List<SongWithMetadata>

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongWithMeta(songId: String): SongWithMetadata?

    data class SongPath(
        val path: String,
        val folderId: Int
    )
    @Query("SELECT path, folder_id AS folderId FROM songs")
    suspend fun getAllSongPaths(): List<SongPath>

    @Transaction
    @Query("SELECT * FROM songs WHERE folder_id = :folderId")
    suspend fun getSongsByFolderId(folderId: Int): List<SongWithMetadata>

    @Transaction
    @Query("SELECT * FROM albums")
    suspend fun getAllAlbumsWithMeta(): List<AlbumsWithMeta>

    @Query("SELECT * FROM albums")
    suspend fun getAllAlbums(): List<Album>

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumWithSongs(albumId: String): AlbumsWithMeta?

    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbum(albumId: String): Album?

    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtists(): List<Artist>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtistsWithMeta(): List<ArtistsWithMeta>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistWithDetails(artistId: String): ArtistsWithMeta?

    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtist(artistId: String): Artist?

    @Transaction
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' LIMIT :limit OFFSET :offset")
    suspend fun searchSongs(query: String, limit: Int, offset: Int): List<SongWithMetadata>

    @Transaction
    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' LIMIT :limit OFFSET :offset")
    suspend fun searchAlbums(query: String, limit: Int, offset: Int): List<AlbumsWithMeta>

    @Transaction
    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' LIMIT :limit OFFSET :offset")
    suspend fun searchArtists(query: String, limit: Int, offset: Int): List<ArtistsWithMeta>

    @Insert(onConflict = REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Insert(onConflict = REPLACE)
    suspend fun insertAlbums(albums: List<Album>)

    @Insert(onConflict = REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()

    @Query("DELETE FROM albums")
    suspend fun clearAllAlbums()

    @Query("DELETE FROM artists")
    suspend fun clearAllArtists()

    // Playlist section
    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylist(id: String): PlaylistWithSongs?

    @Insert(onConflict = REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: String, name: String)

    @Query("UPDATE playlists SET comment = :comment WHERE id = :playlistId")
    suspend fun updatePlaylistComment(playlistId: String, comment: String)

    @Query("UPDATE playlists SET public = :public WHERE id = :playlistId")
    suspend fun updatePlaylistPublic(playlistId: String, public: Boolean)

    @Query("UPDATE playlists SET changed = :date WHERE id = :playlistId")
    suspend fun updatePlaylistDate(playlistId: String, date: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = REPLACE)
    suspend fun insertPlaylistSongs(songs: List<PlaylistSong>)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: String)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: String, songId: String)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND order_index IN (:indices)")
    suspend fun deleteSongsByIndex(playlistId: String, indices: List<Int>)

    @Query("SELECT MAX(order_index) FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun getMaxOrderIndex(playlistId: String): Int?

    //Starring section

    @Insert(onConflict = REPLACE)
    suspend fun addStar(item: States)

    @Query("UPDATE songs SET starred = :starred WHERE id = :id")
    suspend fun updateStarredSong(id: String, starred: Long?)

    @Query("UPDATE albums SET starred = :starred WHERE id = :id")
    suspend fun updateStarredAlbum(id: String, starred: Long?)

    @Query("UPDATE artists SET starred = :starred WHERE id = :id")
    suspend fun updateStarredArtist(id: String, starred: Long?)

    @Query("UPDATE songs SET user_rating = :rating WHERE id = :id")
    suspend fun updateSongRating(id: String, rating: Int?)

    @Query("UPDATE albums SET user_rating = :rating WHERE id = :id")
    suspend fun updateAlbumRating(id: String, rating: Int?)

    @Query("UPDATE artists SET user_rating = :rating WHERE id = :id")
    suspend fun updateArtistRating(id: String, rating: Int?)

    @Query("DELETE FROM states WHERE id = :id")
    suspend fun removeStar(id: String)

    @Query("SELECT * FROM states WHERE starred IS NOT NULL")
    suspend fun getAllStarred(): List<States>

    @Query("SELECT * FROM states WHERE id = :id")
    suspend fun getStarred(id: String): States?

    // Scrobbling section
    @Query("UPDATE states SET playCount = playCount + 1, lastPlayed = :time WHERE id = :id")
    suspend fun addPlayCount(id: String, time: Long)

    @Insert(onConflict = REPLACE)
    suspend fun insertScrobble(item: Scrobble)

    @Query("UPDATE songs SET play_count = play_count + 1, last_played = :time WHERE id = :id")
    suspend fun addSongPlayCount(id: String, time: Long)

    @Query("UPDATE albums SET play_count = play_count + 1, last_played = :time WHERE id = :id")
    suspend fun addAlbumPlayCount(id: String, time: Long)

    @Query("UPDATE artists SET play_count = play_count + 1, last_played = :time WHERE id = :id")
    suspend fun addArtistPlayCount(id: String, time: Long)

    @Query("DELETE FROM scrobbles WHERE expiresAt < :time")
    suspend fun clearScrobbles(time: Long)

    @Query("SELECT * FROM scrobbles WHERE expiresAt > :time")
    suspend fun getScrobbles(time: Long): List<Scrobble>
}