package omega.sunkey.sunkensonic.server

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import omega.sunkey.sunkensonic.server.room.Artist
import omega.sunkey.sunkensonic.server.room.Song
import omega.sunkey.sunkensonic.server.room.Album
import omega.sunkey.sunkensonic.server.room.States
import omega.sunkey.sunkensonic.server.room.SubsonicDatabase
import java.security.MessageDigest

class MediaScanner (
    context: Context,
    p: WorkerParameters
) : CoroutineWorker(context, p) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = SubsonicDatabase.getInstance(applicationContext)
                val dao = db.subsonicDao()
                Log.i("MediaScanner", "scanning !!!!!!!!!!!!!!")

                //setProgress(workDataOf("status" to "Cleaning", "progress" to 0))
                dao.clearAllSongs()
                dao.clearAllAlbums()
                dao.clearAllArtists()

                //setProgress(workDataOf("status" to "Querying files", "progress" to 5))
                val audioFiles = queryAudioFiles()
                Log.i("MediaScanner", "found ${audioFiles.size} !!!! much!")
                //setProgress(workDataOf("status" to "Scanning ${audioFiles.size} files", "progress" to 10))

                val artists = mutableMapOf<String, Artist>()
                val albums = mutableMapOf<String, Album>()
                val songs = mutableListOf<Song>()
                val statesItems = mutableMapOf<String, States>()
                dao.getAllStarred().forEach {
                    statesItems[it.id] = it
                }
                var processedF = 0
                for (file in audioFiles) {
                    val meta = extractMeta(file.uri)
                    val name = meta.artist ?: "Unknown Artist"
                    val year = file.year ?: meta.year
                    val track = file.track ?: meta.track
                    val folderId = file.path.substringBeforeLast("/").substringAfterLast("/").hashCode()
                    val artid = name.md5()
                    if(!artists.containsKey(artid)) {
                        artists[artid] = Artist(artid, name, folderId = folderId)
                    } else if (statesItems.containsKey(artid)) {
                        artists[artid] = Artist(artid, name, statesItems[artid]?.starred, statesItems[artid]?.rating, statesItems[artid]?.playCount ?: 0, statesItems[artid]?.lastPlayed, folderId)
                        statesItems.remove(artid)
                    }
                    val bitrate = file.bitrate ?: ((file.size * 8) / (file.duration / 1000) / 1000 ).toInt()

                    val album = meta.album ?: "Unknown Album"
                    val aid = "${artid}_${album}".md5()
                    if (!albums.containsKey(aid)) {
                        albums[aid] = Album(aid, album, artid, year, file.coverId, file.dateAdded, folderId = folderId)
                    } else if(statesItems.containsKey(aid)) {
                        albums[aid] = Album(aid, album, artid, year, file.coverId, file.dateAdded, statesItems[aid]?.starred, statesItems[aid]?.rating, statesItems[aid]?.playCount ?: 0, statesItems[aid]?.lastPlayed, folderId)
                        statesItems.remove(aid)
                    }
                    
                    val songstate: List<String?> = if(statesItems.containsKey(file.id.toString())) {
                        listOf(
                            statesItems[file.id.toString()]?.starred.toString(),
                            statesItems[file.id.toString()]?.rating.toString(),
                            statesItems[file.id.toString()]?.playCount.toString(),
                            statesItems[file.id.toString()]?.lastPlayed.toString()
                        )
                    } else listOf(null, null, null, null)

                    val song = Song(
                        id = file.id.toString(),
                        title = meta.title ?: file.displayName,
                        albumId = aid,
                        artistId = artid,
                        duration = file.duration / 1000,
                        bitrate = bitrate,
                        path = file.path,
                        size = file.size,
                        track = track,
                        discNumber = file.discNumber,
                        year = year,
                        genre = meta.genre,
                        suffix = file.displayName.substringAfterLast('.', ""),
                        dateAdded = file.dateAdded,
                        coverArt = file.coverId,
                        starred = songstate[0]?.toLongOrNull(),
                        userRating = songstate[1]?.toIntOrNull() ?: 0,
                        playCount = songstate[2]?.toIntOrNull() ?: 0,
                        lastPlayed = songstate[3]?.toLongOrNull(),
                        folderId = folderId
                    )
                    songs.add(song)
                    if (statesItems.containsKey(file.id.toString())) statesItems.remove(file.id.toString())

                    processedF++
                    if(processedF % 10 == 0 || processedF == audioFiles.size) {
                        val p = 10 + ((processedF * 80) / audioFiles.size)
                        //setProgress(workDataOf("status" to "Scanning $processedF / ${audioFiles.size}", "progress" to p))
                    }
                }

                //setProgress(workDataOf("status" to "Indexing everything in database !", "progress" to 95))
                dao.insertArtists(artists.values.toList())
                dao.insertAlbums(albums.values.toList())
                dao.insertSongs(songs)
                if (statesItems.isNotEmpty()) statesItems.forEach { (id, _) -> dao.removeStar(id) }
                Log.i("MediaScanner", "done :D | indexed ${artists.size} artists, ${albums.size} albums, and ${songs.size} songs !!!!")
                //setProgress(workDataOf("status" to "Done !", "progress" to 100))
                Result.success()
            } catch (e: Exception) {
                Log.e("MediaScanner", "oops", e)
                //setProgress(workDataOf("status" to "fail !", "progress" to -1))
                Result.failure()
            }
        }
    }
    private fun queryAudioFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISC_NUMBER
        )
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        applicationContext.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dur = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val bitr = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            val siz = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val date = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val year = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val track = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val discn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISC_NUMBER)

            while (cursor.moveToNext()) {
                val idd = cursor.getLong(id)
                val namee = cursor.getString(name)
                val dura = cursor.getInt(dur)
                val bitrate = cursor.getInt(bitr)?.div(1000)
                val size = cursor.getLong(siz)
                val path = cursor.getString(data)
                val dateAdded = cursor.getLong(date)
                val cUri = ContentUris.withAppendedId(collection, idd)
                val coUri = cursor.getLong(albumId).toString()
                val yearr = cursor.getInt(year)
                val trackk = cursor.getInt(track)
                val discnumber = cursor.getInt(discn)
                audioFiles.add(AudioFile(idd, namee, dura, bitrate, size, path, dateAdded, cUri, coUri, yearr, trackk, discnumber))
            }
        }
        return audioFiles
    }
    private fun extractMeta(uri: Uri): AudioMetadata {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(applicationContext, uri)
            AudioMetadata(
                title = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM),
                track = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull(),
                year = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull(),
                genre = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_GENRE)
            )
        } catch (e: Exception) {
            Log.w("MediaScanner", "Failed to extract metadata from $uri", e)
            AudioMetadata()
        } finally {
            retriever.release()
        }
    }

    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private data class AudioFile(
        val id: Long,
        val displayName: String,
        val duration: Int,
        val bitrate: Int?,
        val size: Long,
        val path: String,
        val dateAdded: Long,
        val uri: Uri,
        val coverId: String,
        val year: Int?,
        val track: Int?,
        val discNumber: Int?
    )
    private data class AudioMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val track: Int? = null,
        val year: Int? = null,
        val genre: String? = null
    )
}
