package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import okhttp3.OkHttpClient
import okhttp3.Request
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.await
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.ArtistInfo
import omega.sunkey.sunkensonic.server.responses.ArtistInfoView
import omega.sunkey.sunkensonic.server.responses.ArtistInfoViewTheSequel
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success
import java.net.URLEncoder

fun getArtistInfo2(context: Context, scope: CoroutineScope, dao: SubsonicDao, lastFmKey: String, client: OkHttpClient) {
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
        if (artist.biography == null) {
            val sanitizedName = URLEncoder.encode(URLEncoder.encode(artist.name, "UTF-8"), "UTF-8")
            val infoRequest = Request.Builder()
                .url("https://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=${lastFmKey}&artist=${sanitizedName}&format=json")
                .build()
            val response = client.newCall(infoRequest).await()
            val json = decodeArtist(response.body.string())
            if (json == null) {
                dao.updateArtistLastFM(
                    id,
                    null,
                    null,
                    null,
                    null,
                    "There is no Last.FM information about this artist"
                )
                success(context, ArtistInfoView(ArtistInfo(
                    "There is no Last.FM information about this artist",
                    null,
                    null,
                    null,
                    null
                )))
            } else {
                dao.updateArtistLastFM(
                    id,
                    json.musicBrainzId,
                    json.smallImageUrl,
                    json.mediumImageUrl,
                    json.largeImageUrl,
                    json.biography
                )
                success(
                    context, ArtistInfoViewTheSequel(json)
                )
            }
            return@future
        } else {
            success(context, ArtistInfoViewTheSequel(ArtistInfo(
                biography = artist.biography,
                musicBrainzId = artist.listenBrainzId,
                smallImageUrl = artist.smallImageUrl,
                mediumImageUrl = artist.mediumImageUrl,
                largeImageUrl = artist.largeImageUrl,
            )))
        }
    })
}