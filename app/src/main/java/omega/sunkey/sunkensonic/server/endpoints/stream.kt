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

fun stream(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.header("Accept-Ranges", "bytes")
    context.future(scope.future {
        val song = dao.getSongWithMeta(id)
        if(song == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        val range = parseRangeHeader(context.header("Range"), song.song.size.toInt())
        sendStream(context, song.song.id, conType(song.song.suffix!!), song.song.size, range, androidContext)
    })
}

fun sendStream(context: Context, id: String, mime: String, size: Long, range: Pair<Int, Int>?, androidContext: android.content.Context) {
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
        val buffer = ByteArray(range.second - range.first)
        inputStream.read(buffer, 0, range.second - range.first)
        context.header("Content-Length", "${buffer.size}")
        context.header("Content-Range", "bytes ${range.first}-${range.second}/$size")
        context.status(206)
        context.result(buffer)
    } else if (inputStream != null) {
        context.header("Content-Length", size.toString())
        context.result(inputStream)
    }
}

fun parseRangeHeader(range: String?, fileSize: Int): Pair<Int, Int>? {
    if (range == null) return null
    val rangeStart = range.removePrefix("bytes=").split("-")[0].toIntOrNull()
    val rangeEnd = range.removePrefix("bytes=").split("-")[1].toIntOrNull()

    return when {
        rangeStart == null && rangeEnd != null -> Pair(maxOf(0, fileSize - rangeEnd), fileSize - 1)
        rangeStart != null && rangeEnd == null && rangeStart <= fileSize -> Pair(rangeStart, fileSize)
        rangeStart != null && rangeEnd != null && rangeStart <= fileSize -> Pair(rangeStart, minOf(rangeEnd, fileSize - 1))
        else -> null
    }
}