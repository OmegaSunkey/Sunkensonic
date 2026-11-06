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

fun star(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    val albumId = context.queryParam("albumId")
    val artistId = context.queryParam("artistId")
    when {
        id != null && albumId != null && artistId != null ||
                id != null && albumId != null ||
                id != null && artistId != null ||
                albumId != null && artistId != null -> {
            reject(context, Reject.GENERIC)
            return
        }
        id == null && albumId == null && artistId == null -> {
            reject(context, Reject.MISSINGPARAM)
            return
        }
    }
    context.future(scope.future {
        val states = dao.getAllStarred().filter { it.id == id }
        val state = if (states.isNotEmpty()) states[0] else when {
            id != null && id.startsWith("so") -> States(id, "song")
            id != null && id.startsWith("ar") -> States(id, "artist")
            id != null && id.startsWith("al") -> States(id, "album")
            else -> null
        }
        if(state == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val star = when {
            id != null && id.startsWith("so", false) -> {
            	state.copy(starred = Clock.System.now().epochSeconds).also {
            		dao.updateStarredSong(it.id, it.starred)
            	}
            }
            id != null && id.startsWith("ar", false) -> {
                state.copy(starred = Clock.System.now().epochSeconds).also {
            		dao.updateStarredArtist(it.id, it.starred)
            	}
            }
            id != null && id.startsWith("al", false) -> {
                state.copy(starred = Clock.System.now().epochSeconds).also {
            		dao.updateStarredAlbum(it.id, it.starred)
            	}
            }
            artistId != null -> {
                state.copy(starred = Clock.System.now().epochSeconds).also {
                    dao.updateStarredArtist(it.id, it.starred)
                }
            }
            albumId != null -> {
                state.copy(starred = Clock.System.now().epochSeconds).also {
                    dao.updateStarredAlbum(it.id, it.starred)
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
