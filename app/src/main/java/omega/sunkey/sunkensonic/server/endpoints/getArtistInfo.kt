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
import omega.sunkey.sunkensonic.server.responses.ArtistInfo
import omega.sunkey.sunkensonic.server.responses.ArtistInfoView
import omega.sunkey.sunkensonic.server.responses.LastFmArtistInfo
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getArtistInfo(context: Context, scope: CoroutineScope, dao: SubsonicDao, lastFmKey: String, client: OkHttpClient) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val artist = dao.getArtist(id)
        if (artist == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        if (artist.listenBrainzId == null) {
            val infoRequest = Request.Builder()
                .url("https://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=${lastFmKey}&artist=${artist.name}&format=json")
                .build()
            val response = client.newCall(infoRequest).await()
            val json = Json.decodeFromString<LastFmArtistInfo>(response.body.string())
            dao.updateArtistLastFM(
                id,
                json.artist.mbid,
                json.artist.image.first { it.size == "small" }.text,
                json.artist.image.first { it.size == "medium" }.text,
                json.artist.image.first { it.size == "large" }.text,
                json.artist.bio.content
            )
            success(context, ArtistInfoView(ArtistInfo(
                biography = json.artist.bio.content,
                musicBrainzId = json.artist.mbid,
                smallImageUrl = json.artist.image.first { it.size == "small" }.text,
                mediumImageUrl = json.artist.image.first { it.size == "medium" }.text,
                largeImageUrl = json.artist.image.first { it.size == "large" }.text,
            )))
            return@future
        } else {
            success(context, ArtistInfoView(ArtistInfo(
                biography = artist.biography!!,
                musicBrainzId = artist.listenBrainzId,
                smallImageUrl = artist.smallImageUrl!!,
                mediumImageUrl = artist.mediumImageUrl!!,
                largeImageUrl = artist.largeImageUrl!!,
            )))
        }
    })
}