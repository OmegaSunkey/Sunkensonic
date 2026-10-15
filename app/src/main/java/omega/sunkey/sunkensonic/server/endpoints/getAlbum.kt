package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumView
import omega.sunkey.sunkensonic.server.responses.SongData
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.toAlbumDataWithMeta
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getAlbum(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaalbum = dao.getAlbumWithSongs(id)
        if (metaalbum == null) {
            reject(context, Reject.NODATA)
            return@future
        }

        success(context, AlbumView(metaalbum.toAlbumDataWithMeta()))
    })
}
