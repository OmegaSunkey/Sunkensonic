package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.NowPlaying
import omega.sunkey.sunkensonic.server.responses.NowPlayingData
import omega.sunkey.sunkensonic.server.responses.NowPlayingView
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import omega.sunkey.sunkensonic.server.toIsoTime
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
                NowPlayingData(
                    metasong.song.id,
                    metasong.album.id,
                    false,
                    metasong.song.title,
                    metasong.album.title,
                    metasong.artist.name,
                    metasong.song.track,
                    metasong.song.year,
                    metasong.song.genre,
                    metasong.song.coverArt,
                    metasong.song.size,
                    conType(metasong.song.suffix!!),
                    metasong.song.suffix,
                    metasong.song.userRating ?: 0,
                    metasong.song.starred?.toIsoTime(),
                    metasong.song.duration,
                    metasong.song.bitrate,
                    metasong.song.path,
                    metasong.song.discNumber,
                    metasong.song.dateAdded.toIsoTime(),
                    metasong.song.albumId,
                    metasong.song.artistId,
                    username = it.username,
                    minutesAgo = ((Clock.System.now().toEpochMilliseconds() - it.startedAt) / 60000).toInt(),
                    playerId = 0
                )
            )
        }
        success(context, NowPlayingView(NowPlaying(songDataList)))
    })
}