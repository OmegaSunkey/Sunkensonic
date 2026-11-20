package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.await
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.AlbumInfo
import omega.sunkey.sunkensonic.server.responses.AlbumInfoView
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
        if (album.album.notes == null) {
            val infoRequest = Request.Builder()
                .url("https://ws.audioscrobbler.com/2.0/?method=album.getInfo&api_key=${lastFmKey}&artist=${album.artist.name}&album=${album.album.title}&format=json")
                .build()
            val response = client.newCall(infoRequest).await()
            val json = decodeAlbum(response.body.string())
            if (json == null) {
                dao.updateAlbumLastFM(id, null, null, null, null, "There is no Last.FM information about this album")
                success(context, AlbumInfoView(AlbumInfo(
                    notes = "There is no Last.FM information about this album",
                    null,
                    null,
                    null,
                    null
                )))
            } else {
                dao.updateAlbumLastFM(
                    id,
                    json.musicBrainzId,
                    json.smallImageUrl,
                    json.mediumImageUrl,
                    json.largeImageUrl,
                    json.notes
                )
                success(
                    context, AlbumInfoView(json)
                )
            }
            return@future
        } else {
            success(context, AlbumInfoView(AlbumInfo(
                notes = album.album.notes,
                musicBrainzId = album.album.listenBrainzId,
                smallImageUrl = album.album.smallImageUrl,
                mediumImageUrl = album.album.mediumImageUrl,
                largeImageUrl = album.album.largeImageUrl
            )))
        }
    })
}

fun decodeAlbum(json: String): AlbumInfo? {
    val jelement = Json.parseToJsonElement(json)
    return if(jelement is JsonObject && "error" in jelement) {
        null
    } else {
        val jalbum = jelement.jsonObject["album"]!!.jsonObject
        AlbumInfo(
            notes = jalbum["wiki"]?.jsonObject["content"]?.jsonPrimitive?.contentOrNull ?: "There is no Last.FM information about this album",
            musicBrainzId = jalbum["mbid"]?.jsonPrimitive?.contentOrNull,
            smallImageUrl = jalbum["image"]?.jsonArray[0]?.jsonObject["#text"]?.jsonPrimitive?.contentOrNull,
            mediumImageUrl = jalbum["image"]?.jsonArray[1]?.jsonObject["#text"]?.jsonPrimitive?.contentOrNull,
            largeImageUrl = jalbum["image"]?.jsonArray[2]?.jsonObject["#text"]?.jsonPrimitive?.contentOrNull
        )
    }
}