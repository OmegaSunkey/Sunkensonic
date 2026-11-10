package omega.sunkey.sunkensonic.server

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.javalin.http.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


fun reject(ctx: Context, reason: Reject) {
    val response = FailedResponse(
        error = Error(reason.code, reason.message)
    )
    val root = Root(response)
    val f = ctx.queryParam("f")
    ctx.status(200)

    if(f == "json") {
        ctx.contentType("application/json")
        ctx.json(root)
    } else {
        ctx.contentType("application/xml")
        val xml = XmlMapper()
        ctx.result(xml.writeValueAsString(root))
    }
}

fun <T> success(ctx: Context, payload: T? = null) {
    val response = Response(
        status = "ok",
        payload = payload
    )
    val root = Root(response)
    val f = ctx.queryParam("f")
    ctx.status(200)

    if (f == "json") {
        ctx.contentType("application/json")
        ctx.json(root)
    } else {
        ctx.contentType("application/xml")
        val xml = XmlMapper()
        ctx.result(xml.writeValueAsString(root))
    }
}

enum class Reject(val code: Int, val message: String) {
    GENERIC(0, "Something happened."),
    MISSINGPARAM(10, "The required parameter is missing."),
    INCOMPATCLIENT(20, "Incompatible Subsonic REST protocol version. Client must upgrade."),
    INCOMPATSERVER(30, "Incompatible Subsonic REST protocol version. Server must upgrade."),
    WRONGAUTH(40, "Wrong username or password"),
    NOTOKEN(41, "Token authentication not supported for LDAP users."),
    INCOMPATAUTH(42, "Provided authentication mechanism not supported."),
    MULTIAUTH(43, "Multiple conflicting authentication mechanisms provided"),
    INVALIDKEY(44, "Invalid API key."),
    NOAUTH(50, "User is not authorized for the given operation"),
    NODATA(70, "The requested data was not found.")
}

// Source - https://stackoverflow.com/a/79756792
// Posted by charlag
// Retrieved 2026-11-10, License - CC BY-SA 4.0


internal suspend inline fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        val callback: Callback = object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        }
        this.enqueue(callback)
        continuation.invokeOnCancellation { this.cancel() }
    }
}
