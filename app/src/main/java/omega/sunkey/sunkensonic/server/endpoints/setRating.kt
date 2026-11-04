package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.States
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import kotlin.time.Clock

fun setRating(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    val albumId = context.queryParam("albumId")
    val artistId = context.queryParam("artistId")
    var rating = context.queryParam("rating")?.toIntOrNull()
    when {
        id != null && albumId != null && artistId != null ||
                id != null && albumId != null ||
                id != null && artistId != null ||
                albumId != null && artistId != null -> {
            reject(context, Reject.GENERIC)
            return
        }
        id == null && albumId == null && artistId == null && rating == null -> {
            reject(context, Reject.MISSINGPARAM)
            return
        }
        rating != null && rating >= 5 -> {
            rating = 5
        }
    }
    context.future(scope.future {
        val star = when {
            id != null && rating != 0 -> {
                States(id, "song", Clock.System.now().epochSeconds, rating).also {
                    dao.updateSongRating(it.id, it.rating)
                }
            }
            artistId != null && rating != 0 -> {
                States(artistId, "artist", Clock.System.now().epochSeconds, rating).also {
                    dao.updateArtistRating(it.id, it.rating)
                }
            }
            albumId != null && rating != 0 -> {
                States(albumId, "artist", Clock.System.now().epochSeconds, rating).also {
                    dao.updateAlbumRating(it.id, it.rating)
                }
            }
            id != null -> {
                States(id, "song", Clock.System.now().epochSeconds).also {
                    dao.updateSongRating(it.id, null)
                }
            }
            artistId != null -> {
                States(artistId, "artist", Clock.System.now().epochSeconds).also {
                    dao.updateArtistRating(it.id, null)
                }
            }
            albumId != null -> {
                States(albumId, "artist", Clock.System.now().epochSeconds).also {
                    dao.updateAlbumRating(it.id, null)
                }
            }
            else -> null
        }
        if (star != null) {
            dao.addStar(star)
            success(context, null)
        } else {
            reject(context, Reject.GENERIC)
            return@future
        }
    })
}