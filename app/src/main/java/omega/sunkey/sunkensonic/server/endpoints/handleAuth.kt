package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.Javalin
import omega.sunkey.sunkensonic.server.Reject
import omega.sunkey.sunkensonic.server.md5
import omega.sunkey.sunkensonic.server.reject
import java.net.URLDecoder

class AuthException(val code: Reject, override val message: String) : Exception(message)

fun Javalin.setBeforeHandler() {
    this.exception(AuthException::class.java) { e, ctx ->
        reject(ctx, e.code)
    }

    this.before("/rest/*") { ctx ->
        if(ctx.path().contains("ping")) return@before
        val u = ctx.queryParam("u")
        val p = ctx.queryParam("p")
        val t: String? = ctx.queryParam("t")
        val s: String? = ctx.queryParam("s")

        if(u == null) {
            throw AuthException(Reject.MISSINGPARAM, Reject.MISSINGPARAM.message)
        }

        //TODO: actual user management
        val auth = when {
            t != null && s != null -> t == ("sunkey$s").md5()
            p != null && p.startsWith("enc:") -> encDecode(p)
            p != null -> p == "sunkey"
            else -> false
        }

        if(u != "sunkey" && auth) {
            throw AuthException(Reject.WRONGAUTH, Reject.WRONGAUTH.message)
        } else ctx.attribute("currentUser", u)

        ctx.attribute("client", ctx.queryParam("c") ?: "Unknown Client")
    }
}

fun encDecode(p: String): Boolean {
    try {
        val bytes = ByteArray(p.length / 2)
        for (i in bytes.indices) {
            val index = i * 2
            val v = p.substring(index, index + 2).toInt(16)
            bytes[i] = v.toByte()
        }
        return String(bytes, Charsets.UTF_8) == "sunkey"
    } catch (_: Exception) {
        return URLDecoder.decode(p, "UTF-8") == "sunkey"
    }
}