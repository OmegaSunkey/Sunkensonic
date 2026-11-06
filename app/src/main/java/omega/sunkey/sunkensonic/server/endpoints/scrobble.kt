package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.Scrobble
import omega.sunkey.sunkensonic.server.room.SongWithMetadata
import omega.sunkey.sunkensonic.server.room.States
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import kotlin.time.Clock

fun scrobble(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val ids = context.queryParams("id")
    val times = context.queryParams("time")
    val submission = context.queryParam("submission")?.toBooleanStrictOrNull()

    if (ids.isEmpty()) {
        reject(context, Reject.MISSINGPARAM)
        return
    }

    context.future(scope.future {
        if (ids.size == times.size) {
            ids.zip(times).forEach { (id, time) ->
                val now = time.toLongOrNull() ?: Clock.System.now().toEpochMilliseconds()
                val metasong = dao.getSongWithMeta(id) ?: return@forEach
                if (submission == true) {
                    dao.addPlayCount(id, now)
                    dao.addSongPlayCount(id, now)
                    dao.addAlbumPlayCount(metasong.album.id, now)
                    dao.addArtistPlayCount(metasong.artist.id, now)
                    updateState(metasong, dao)
                } else {
                    dao.insertScrobble(
                        Scrobble(
                            id = id,
                            username = context.attribute<String>("currentUser")!!,
                            startedAt = now,
                            expiresAt = now + ((metasong.song.duration) * 1000)
                        )
                    )
                }
            }
        } else {
            ids.forEach {
                val metasong = dao.getSongWithMeta(it) ?: return@forEach
                if (submission == true) {
                    dao.addPlayCount(it, Clock.System.now().toEpochMilliseconds())
                    dao.addSongPlayCount(it, Clock.System.now().toEpochMilliseconds())
                    dao.addAlbumPlayCount(metasong.album.id, Clock.System.now().toEpochMilliseconds())
                    dao.addArtistPlayCount(metasong.artist.id, Clock.System.now().toEpochMilliseconds())
                    updateState(metasong, dao)
                } else {
                    val now = if (times.isNotEmpty()) {
                        times[0].toLongOrNull() ?: Clock.System.now().toEpochMilliseconds()
                    } else Clock.System.now().toEpochMilliseconds()
                    dao.insertScrobble(
                        Scrobble(
                            id = it,
                            username = context.attribute<String>("currentUser")!!,
                            startedAt = now,
                            expiresAt = now + ((metasong.song.duration) * 1000)
                        )
                    )
                }
            }
        }
        success(context, null)
    })
}

suspend fun updateState(song: SongWithMetadata, dao: SubsonicDao) {
    val artistState = dao.getStarred(song.artist.id)
    val albumState = dao.getStarred(song.album.id)
    val songState = dao.getStarred(song.song.id)

    if (artistState != null) {
        dao.addStar(
            artistState.copy(
                playCount = artistState.playCount+1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    } else {
        dao.addStar(
            States(
                song.artist.id,
                "artist",
                playCount = 1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    }
    if (albumState != null) {
        dao.addStar(
            albumState.copy(
                playCount = albumState.playCount+1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    } else {
        dao.addStar(
            States(
                song.album.id,
                "album",
                playCount = 1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    }
    if (songState != null) {
        dao.addStar(
            songState.copy(
                playCount = songState.playCount+1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    } else {
        dao.addStar(
            States(
                song.song.id,
                "song",
                playCount = 1,
                lastPlayed = Clock.System.now().toEpochMilliseconds()
            )
        )
    }
}