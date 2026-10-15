package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.NowPlaying
import omega.sunkey.sunkensonic.server.responses.NowPlayingData
import omega.sunkey.sunkensonic.server.responses.NowPlayingView
import omega.sunkey.sunkensonic.server.responses.toNowPlayingData
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getNowPlaying(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        dao.clearScrobbles(Clock.System.now().toEpochMilliseconds())
        val nowplayingList = dao.getScrobbles(Clock.System.now().toEpochMilliseconds())
        val songDataList = mutableListOf<NowPlayingData>()
        nowplayingList.forEach {
            val metasong = dao.getSongWithMeta(it.id) ?: return@forEach
            songDataList.add(
                metasong.toNowPlayingData(it.username, it.startedAt)
            )
        }
        success(context, NowPlayingView(NowPlaying(songDataList)))
    })
}