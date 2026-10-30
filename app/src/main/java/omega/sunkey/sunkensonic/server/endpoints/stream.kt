package omega.sunkey.sunkensonic.server.endpoints

import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import kotlin.text.removePrefix

fun stream(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.header("Accept-Ranges", "bytes")
    context.future(scope.future {
        val song = dao.getSong(id)
        if(song == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        sendStream(context, song.id, conType(song.suffix!!), song.size, context.header("Range"), androidContext)
    })
}

fun sendStream(context: Context, id: String, mime: String, size: Long, range: String?, androidContext: android.content.Context) {
    context.contentType(mime)

    val mediaURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val uri = ContentUris.withAppendedId(
        mediaURI,
        id.toLong()
    )
    val inputStream = androidContext.contentResolver.openInputStream(uri)
    if (inputStream != null && range != null) {
        val rangeStart = range.removePrefix("bytes=").split("-")[0].toIntOrNull()
        val rangeEnd = range.removePrefix("bytes=").split("-")[1].toIntOrNull()
        var buffer: ByteArray
        when {
            /* 8196
            * Last 500 Bytes
            * Range: bytes=-500
            * 8196-500 = 7696
            * offset of 7696, length of 500
            */
            rangeStart == null && rangeEnd != null -> {
                buffer = ByteArray(rangeEnd)
                inputStream.skip(maxOf(0, size - rangeEnd))
                inputStream.read(buffer, 0, rangeEnd)
                inputStream.close()
            }
            /* Last 500 bytes
            * Range: bytes=7696-
            * 8196-7696 = 500
            * offset of 7696, length of 500
            */
            rangeStart != null && rangeEnd == null -> {
                buffer = ByteArray(maxOf(0, size - rangeStart).toInt())
                inputStream.skip(rangeStart.toLong())
                inputStream.read(buffer, 0, minOf(size, size - rangeStart).toInt())
                inputStream.close()
            }
            /* Range of bytes
            * Range: bytes=400-5667 / 8196
            * 5667-400 = 5267
            * offset of 400, length of 5267
            */
            rangeStart != null && rangeEnd != null -> {
                buffer = ByteArray(rangeEnd - rangeStart)
                inputStream.skip(rangeStart.toLong())
                inputStream.read(buffer, 0, rangeEnd - rangeStart)
                inputStream.close()
            }
            else -> {
                reject(context, Reject.GENERIC)
                return
            }
        }
        context.header("Content-Length", "${buffer.size}")
        context.header("Content-Range", "bytes ${rangeStart}-${rangeEnd}/$size")
        context.status(206)
        context.result(buffer)
    } else if (inputStream != null) {
        context.header("Content-Length", size.toString())
        context.result(inputStream)
    } else {
        reject(context, Reject.GENERIC)
        return
    }
}