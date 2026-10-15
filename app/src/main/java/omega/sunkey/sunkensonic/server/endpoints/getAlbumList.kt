package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.AlbumData
import omega.sunkey.sunkensonic.server.responses.AlbumListView1
import omega.sunkey.sunkensonic.server.responses.AlbumListView2
import omega.sunkey.sunkensonic.server.responses.toAlbumData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getAlbumList(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    val type = context.queryParam("type") // lets ignore for now
    context.future(scope.future {
        val allAlbums = dao.getAllAlbumsWithMeta()
        val albumList = mutableListOf<AlbumData>()
        allAlbums.forEach {
            var totalDuration = 0
            it.songs.forEach {
                totalDuration += it.duration
            }
            albumList.add(
                it.toAlbumData(totalDuration)
            )
        }
        success(context, AlbumListView1(AlbumListView2(albumList)))
    })
}
