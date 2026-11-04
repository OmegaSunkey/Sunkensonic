package omega.sunkey.sunkensonic.server.endpoints

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.util.Log
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import java.io.InputStream
import androidx.core.net.toUri
import omega.sunkey.sunkensonic.server.Reject
import java.io.FileNotFoundException

fun getCoverArt(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    Log.i("endpoint", context.fullUrl())
    context.future(scope.future {
        val cover = getCoverArt(androidContext, id, dao, context)
        // using regular context.result(inputStream) doesn't work i dont know why, jackson explodes trying to serialize something
        if(cover != null) {
            context.header("Content-Type", contentType(getCoverArt(androidContext, id, dao, context)))
            cover.use { input -> // i asked claude and it told me to override internal result object so epic
                context.res.outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            reject(context, Reject.NODATA)
        }
    })
}

suspend fun getCoverArt(androidContext: android.content.Context, id: String, dao: SubsonicDao, context: Context): InputStream? {
    val uri = if (id.toLongOrNull() != null) {
        ContentUris.withAppendedId("content://media/external/audio/albumart/".toUri(), id.toLong())
    } else {
        val playlist = dao.getPlaylist(id)
        if (playlist == null || playlist.songs?.isEmpty() == true) {
            reject(context, Reject.NODATA)
            return null
        }
        ContentUris.withAppendedId("content://media/external/audio/albumart/".toUri(), playlist.songs!![0].song.coverArt.toLong())
    }
    Log.i("getCoverArt", "Uri: $uri")
    return try {
        androidContext.contentResolver.openInputStream(uri)
    } catch (_: FileNotFoundException) {
        null
    }
}

fun contentType(image: InputStream?): String {
    if(image == null) return ""
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(image, null, options)
    return options.outMimeType ?: "image/jpeg"
}
