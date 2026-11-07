package omega.sunkey.sunkensonic.server

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simplecityapps.ktaglib.KTagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import omega.sunkey.sunkensonic.SunkensonicApplication
import omega.sunkey.sunkensonic.server.room.Album
import omega.sunkey.sunkensonic.server.room.Artist
import omega.sunkey.sunkensonic.server.room.Song
import omega.sunkey.sunkensonic.server.room.States
import omega.sunkey.sunkensonic.server.room.SubsonicDatabase

class KTagLibMediaScanner(
    context: Context,
    p: WorkerParameters
) : CoroutineWorker(context, p) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = SubsonicDatabase.getInstance(applicationContext)
                val dao = db.subsonicDao()

                val settings = (applicationContext as SunkensonicApplication).settings
                val blacklist = settings.dirBlacklist.read()

                Log.i("MediaScanner", "scanning !!!!!!!!!!!!!!")

                setProgress(workDataOf("status" to "Cleaning", "progress" to 0))
                dao.clearAllSongs()
                dao.clearAllAlbums()
                dao.clearAllArtists()

                setProgress(workDataOf("status" to "Querying files", "progress" to 5))
                val audioFiles = queryAudioFiles().apply {
                    removeIf { audio -> !blacklist.isNullOrEmpty() && blacklist.any { it in audio.path } }
                    toList()
                }
                Log.i("MediaScanner", "found ${audioFiles.size} !!!! much!")
                setProgress(workDataOf("status" to "Scanning ${audioFiles.size} files", "progress" to 10))

                val artists = mutableMapOf<String, Artist>()
                val albums = mutableMapOf<String, Album>()
                val songs = mutableListOf<Song>()
                val statesItems = mutableMapOf<String, States>()
                dao.getAllStarred().forEach {
                    statesItems[it.id] = it
                }
                var processedF = 0
                val mediaURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                for (file in audioFiles) {
                    val uri = ContentUris.withAppendedId(
                        mediaURI,
                        file.id
                    )
                    applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        val metadata = KTagLib().getMetadata(pfd.fd, file.path.substringAfterLast("/"))
                        if (metadata != null) { // most of this is taken from the example in the ktaglib repo:
                            // https://github.com/timusus/KTagLib/blob/release/sample/src/main/java/com/simplecityapps/ktaglib/sample/AudioFile.kt
                            val artName = metadata.propertyMap["ARTIST"]?.firstOrNull() ?: "Unknown Artist"
                            val year = metadata.propertyMap["DATE"]?.firstOrNull() ?: "0"
                            val track = metadata.propertyMap["TRACKNUMBER"]?.firstOrNull()?.substringBefore("/")?.toIntOrNull() ?: 0
                            val folderId = file.path.substringBeforeLast("/").hashCode()
                            val artid = "ar-" + artName.md5()
                            val id = "so-" + file.id.toString()

                            if (!artists.containsKey(artid)) {
                                if (statesItems.containsKey(artid)) {
                                    Log.i("KTagLib MediaScanner", "Applying existing artist state")
                                    artists[artid] = Artist(
                                        artid,
                                        artName,
                                        statesItems[artid]?.starred,
                                        statesItems[artid]?.rating,
                                        statesItems[artid]?.playCount ?: 0,
                                        statesItems[artid]?.lastPlayed,
                                        folderId
                                    )
                                    statesItems.remove(artid)
                                } else {
                                    Log.i("KTagLib MediaScanner", "Creating new artist state")
                                    artists[artid] = Artist(
                                        artid,
                                        artName,
                                        folderId = folderId
                                    )
                                }
                            }

                            val album = metadata.propertyMap["ALBUM"]?.firstOrNull() ?: "Unknown Album"
                            val aid = "al-" + "${artid}_${album}".md5()
                            if (!albums.containsKey(aid)) {
                                if(statesItems.containsKey(aid)) {
                                    Log.i("KTagLib MediaScanner", "Applying existing album state")
                                    albums[aid] = Album(
                                        aid,
                                        album,
                                        artid,
                                        year.toIntOrNull() ?: 0,
                                        file.coverId,
                                        file.dateAdded,
                                        statesItems[aid]?.starred,
                                        statesItems[aid]?.rating,
                                        statesItems[aid]?.playCount ?: 0,
                                        statesItems[aid]?.lastPlayed,
                                        folderId
                                    )
                                    statesItems.remove(aid)
                                } else {
                                    Log.i("KTagLib MediaScanner", "Creating new album state")
                                    albums[aid] = Album(
                                        aid,
                                        album,
                                        artid,
                                        year.toIntOrNull() ?: 0,
                                        file.coverId,
                                        file.dateAdded,
                                        folderId = folderId
                                    )
                                }
                            }

                            val songstate: List<String?> = if(statesItems.containsKey(file.id.toString())) {
                                Log.i("KTagLib MediaScanner", "Applying existing state")
                                listOf(
                                    statesItems[id]?.starred.toString(),
                                    statesItems[id]?.rating.toString(),
                                    statesItems[id]?.playCount.toString(),
                                    statesItems[id]?.lastPlayed.toString()
                                )
                            } else {
                                Log.i("KTagLib MediaScanner", "Creating new song state")
                                listOf(null, null, null, null)
                            }

                            val bitrate = metadata.audioProperties?.bitrate
                                ?: if(file.duration != null)
                                        ((file.size * 8) / (file.duration.toFloat() / 1000.0f) / 1000 ).toInt()
                                else 0
                            val title = metadata.propertyMap["TITLE"]?.firstOrNull() ?: file.displayName
                            val duration = metadata.audioProperties?.duration?.div(1000)
                                ?: file.duration?.div(1000) ?: 0
                            val discNumber = metadata.propertyMap["DISCNUMBER"]?.firstOrNull()?.substringBefore('/')?.toIntOrNull() ?: 0
                            val genre = metadata.propertyMap["GENRE"]?.firstOrNull() ?: "Unknown Genre"

                            val song = Song(
                                id = id,
                                title = title,
                                albumId = aid,
                                artistId = artid,
                                duration = duration,
                                bitrate = bitrate,
                                path = file.path,
                                size = file.size,
                                track = track,
                                discNumber = discNumber,
                                year = year.toIntOrNull() ?: 0,
                                genre = genre,
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

                        } else continue
                    }
                    processedF++
                    val p = 10 + ((processedF * 80) / audioFiles.size)
                    setProgress(workDataOf("status" to "Scanning $processedF / ${audioFiles.size}", "progress" to p))
                }
                setProgress(workDataOf("status" to "Indexing everything in database !", "progress" to 95))
                dao.insertArtists(artists.values.toList())
                dao.insertAlbums(albums.values.toList())
                dao.insertSongs(songs)

                Log.i(
                    "KTagLib MediaScanner",
                    "done :D | indexed ${artists.size} artists, ${albums.size} albums, and ${songs.size} songs !!!!"
                )
                setProgress(workDataOf("status" to "Done !", "progress" to 100))
                Result.success()
            } catch (e: Exception) {
                Log.e("KTagLib MediaScanner", "oops", e)
                setProgress(workDataOf("status" to "fail !", "progress" to -1))
                Result.failure()
            }
        }
    }
    private fun queryAudioFiles(): MutableList<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayListOf(
            MediaStore.Audio.Media._ID, //Since support starts on API 28, some of these have to go to another block
            MediaStore.Audio.Media.DISPLAY_NAME,
            //MediaStore.Audio.Media.DURATION, // API 29
            MediaStore.Audio.Media.SIZE, // API 1
            MediaStore.Audio.Media.DATA, // API 1
            MediaStore.Audio.Media.DATE_ADDED, // API 1
            MediaStore.Audio.Media.ALBUM_ID, // API 1
        ).apply {
            // https://github.com/FoedusProgramme/Gramophone/blob/beta/app/src/main/java/uk/akane/libphonograph/reader/Reader.kt
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.DURATION)
            }
        }.toTypedArray()
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        applicationContext.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dur = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION) else null
            val name = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val siz = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val date = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val idd = cursor.getLong(id)
                val dura = dur?.let { col -> cursor.getInt(col) }
                val displayname = cursor.getString(name)
                val size = cursor.getLong(siz)
                val path = cursor.getString(data)
                val dateAdded = cursor.getLong(date)
                val cUri = ContentUris.withAppendedId(collection, idd)
                val coverUri = cursor.getLong(albumId).toString()
                audioFiles.add(
                    AudioFile(
                        idd,
                        dura,
                        displayname,
                        size,
                        path,
                        dateAdded,
                        cUri,
                        coverUri,
                    )
                )
            }
        }
        return audioFiles
    }
    private data class AudioFile(
        val id: Long,
        val duration: Int?,
        val displayName: String,
        val size: Long,
        val path: String,
        val dateAdded: Long,
        val uri: Uri,
        val coverId: String,
    )
}
