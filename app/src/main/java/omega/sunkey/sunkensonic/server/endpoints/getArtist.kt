package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.ArtistData
import omega.sunkey.sunkensonic.server.responses.SingleArtistView
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.toArtistDataWithMeta
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime

fun getArtist(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val metaartist = dao.getArtistWithDetails(id)
        if (metaartist == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        success(context, SingleArtistView(metaartist.toArtistDataWithMeta()))
    })
}