package omega.sunkey.sunkensonic.server.endpoints

import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.reject
import omega.sunkey.sunkensonic.server.responses.conType
import omega.sunkey.sunkensonic.server.room.SubsonicDao

fun download(context: Context, scope: CoroutineScope, dao: SubsonicDao, androidContext: android.content.Context) {
    val id = context.queryParam("id")
    if(id == null) {
        reject(context, Reject.MISSINGPARAM)
        return
    }
    context.future(scope.future {
        val song = dao.getSong(id)
        if(song == null) {
            reject(context, Reject.NODATA)
            return@future
        }
        getFile(context, song.id, conType(song.suffix!!), song.size, androidContext)
    })
}

fun getFile(context: Context, id: String, mime: String, size: Long, androidContext: android.content.Context) {
    context.contentType(mime)

    val mediaURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val uri = ContentUris.withAppendedId(
        mediaURI,
        id.removePrefix("so-").toLong()
    )
    val inputStream = androidContext.contentResolver.openInputStream(uri)
    if (inputStream != null) {
        context.header("Content-Length", size.toString())
        context.result(inputStream)
    } else {
        reject(context, Reject.GENERIC)
        return
    }
}