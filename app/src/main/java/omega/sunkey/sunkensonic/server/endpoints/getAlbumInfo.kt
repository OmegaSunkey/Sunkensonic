package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.await
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.AlbumInfo
import omega.sunkey.sunkensonic.server.responses.AlbumInfoView
import omega.sunkey.sunkensonic.server.responses.LastFmAlbumInfo
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getAlbumInfo(context: Context, scope: CoroutineScope, dao: SubsonicDao, lastFmKey: String, client: OkHttpClient) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val album = dao.getAlbumWithSongs(id)
        if (album == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        if (album.album.listenBrainzId == null) {
            val infoRequest = Request.Builder()
                .url("https://ws.audioscrobbler.com/2.0/?method=album.getInfo&api_key=${lastFmKey}&artist=${album.artist.name}&album=${album.album.title}&format=json")
                .build()
            val response = client.newCall(infoRequest).await()
            val json = Json.decodeFromString<LastFmAlbumInfo>(response.body.string())
            dao.updateAlbumLastFM(
                id,
                json.album.mbid,
                json.album.image.first { it.size == "small" }.text,
                json.album.image.first { it.size == "medium" }.text,
                json.album.image.first { it.size == "large" }.text
            )
            success(context, AlbumInfoView(AlbumInfo(
                musicBrainzId = json.album.mbid,
                smallImageUrl = json.album.image.first { it.size == "small" }.text,
                mediumImageUrl = json.album.image.first { it.size == "medium" }.text,
                largeImageUrl = json.album.image.first { it.size == "large" }.text
            )))
            return@future
        } else {
            success(context, AlbumInfoView(AlbumInfo(
                musicBrainzId = album.album.listenBrainzId,
                smallImageUrl = album.album.smallImageUrl!!,
                mediumImageUrl = album.album.mediumImageUrl!!,
                largeImageUrl = album.album.largeImageUrl!!
            )))
        }
    })
}