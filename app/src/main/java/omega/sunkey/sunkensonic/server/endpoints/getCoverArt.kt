package omega.sunkey.sunkensonic.server.endpoints

import android.content.ContentUris
import android.graphics.Bitmap
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
    val size = context.queryParam("size")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val cover = when(id.substringBefore("-")) {
            "so" -> {
                dao.getSong(id)?.coverArt
            }
            "ar" -> {
                dao.getArtistWithDetails(id)?.albums[0]?.album?.coverArt
            }
            "al" -> {
                dao.getAlbum(id)?.coverArt
            }
            "pl" -> {
                dao.getPlaylist(id)?.songs?.get(0)?.song?.coverArt
            }
            else -> {
                if(id.toLongOrNull() != null) id else null
            }
        }
        if (cover == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val uri = ContentUris.withAppendedId("content://media/external/audio/albumart/".toUri(), cover.toLong())
        try {
            val inputStream = androidContext.contentResolver.openInputStream(uri)
            val inputStream2 = androidContext.contentResolver.openInputStream(uri)
            if (inputStream != null && inputStream2 != null) {
                if (size?.toIntOrNull() != null) {
                    context.contentType(contentType(inputStream))
                    Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream2), size.toInt(), size.toInt(), true).compress(Bitmap.CompressFormat.JPEG, 80, context.res.outputStream)
                } else {
                    context.contentType(contentType(inputStream))
                    context.result(resultStream = inputStream2)
                }
            } else {
                reject(context, Reject.NODATA)
                return@future
            }
        } catch (e: FileNotFoundException) {
            Log.e("getCoverArt", e.toString())
            reject(context, Reject.NODATA)
            return@future
        }
    })
}
fun contentType(image: InputStream?): String {
    if(image == null) return ""
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeStream(image, null, options)
    return options.outMimeType ?: "image/jpeg"
}