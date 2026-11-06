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
    //val albumId = context.queryParam("albumId")
    //val artistId = context.queryParam("artistId")
    var rating = context.queryParam("rating")?.toIntOrNull()
    when {
        id == null || rating == null -> {
            reject(context, Reject.MISSINGPARAM)
            return
        }
        rating >= 5 -> {
            rating = 5
        }
    }
    context.future(scope.future {
        val states = dao.getAllStarred().filter { it.id == id }
        val state = if (states.isNotEmpty()) states[0] else when {
            id.startsWith("so") -> States(id, "song", Clock.System.now().epochSeconds)
            id.startsWith("ar") -> States(id, "artist", Clock.System.now().epochSeconds)
            id.startsWith("al") -> States(id, "album", Clock.System.now().epochSeconds)
            else -> null
        }
        if(state == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val star = when {
            id.startsWith("so", false) && rating != 0 -> {
                state.copy(rating = rating).also {
                    dao.updateSongRating(it.id, it.rating)
                }
            }
            id.startsWith("ar", false) && rating != 0 -> {
                state.copy(rating = rating).also {
                    dao.updateArtistRating(it.id, it.rating)
                }
            }
            id.startsWith("al", false) && rating != 0 -> {
                state.copy(rating = rating).also {
                    dao.updateAlbumRating(it.id, it.rating)
                }
            }
            id.startsWith("so", false) -> {
                state.copy(rating = null).also {
                    dao.updateSongRating(it.id, null)
                }
            }
            id.startsWith("ar", false) -> {
                state.copy(rating = null).also {
                    dao.updateArtistRating(it.id, null)
                }
            }
            id.startsWith("al", false) -> {
                state.copy(rating = null).also {
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