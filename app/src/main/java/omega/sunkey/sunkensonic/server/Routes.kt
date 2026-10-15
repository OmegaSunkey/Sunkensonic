package omega.sunkey.sunkensonic.server

import android.content.Context
import android.util.Log
import io.javalin.Javalin
import kotlinx.coroutines.CoroutineScope
import omega.sunkey.sunkensonic.server.responses.License
import omega.sunkey.sunkensonic.server.responses.LicenseView
import omega.sunkey.sunkensonic.server.responses.OpenSubsonicExtensionsPayload
import omega.sunkey.sunkensonic.server.endpoints.createPlaylist
import omega.sunkey.sunkensonic.server.endpoints.deletePlaylist
import omega.sunkey.sunkensonic.server.endpoints.getAlbum
import omega.sunkey.sunkensonic.server.endpoints.getAlbumList
import omega.sunkey.sunkensonic.server.endpoints.getAlbumList2
import omega.sunkey.sunkensonic.server.endpoints.getArtist
import omega.sunkey.sunkensonic.server.endpoints.getArtists
import omega.sunkey.sunkensonic.server.endpoints.getCoverArt
import omega.sunkey.sunkensonic.server.endpoints.getGenres
import omega.sunkey.sunkensonic.server.endpoints.getIndexes
import omega.sunkey.sunkensonic.server.endpoints.getMusicDirectory
import omega.sunkey.sunkensonic.server.endpoints.getMusicFolders
import omega.sunkey.sunkensonic.server.endpoints.getNowPlaying
import omega.sunkey.sunkensonic.server.endpoints.getPlaylist
import omega.sunkey.sunkensonic.server.endpoints.getPlaylists
import omega.sunkey.sunkensonic.server.endpoints.getSong
import omega.sunkey.sunkensonic.server.endpoints.getStarred
import omega.sunkey.sunkensonic.server.endpoints.getStarred2
import omega.sunkey.sunkensonic.server.endpoints.getUser
import omega.sunkey.sunkensonic.server.endpoints.getUsers
import omega.sunkey.sunkensonic.server.endpoints.scrobble
import omega.sunkey.sunkensonic.server.endpoints.search3
import omega.sunkey.sunkensonic.server.endpoints.setRating
import omega.sunkey.sunkensonic.server.endpoints.star
import omega.sunkey.sunkensonic.server.endpoints.stream
import omega.sunkey.sunkensonic.server.endpoints.unstar
import omega.sunkey.sunkensonic.server.endpoints.updatePlaylist
import omega.sunkey.sunkensonic.server.room.SubsonicDatabase
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Javalin.setPaths(context: Context, scope: CoroutineScope) {
    val db = SubsonicDatabase.getInstance(context)
    val dao = db.subsonicDao()

    this.get("/rest/ping*") { ctx ->
        success(ctx, null)
    }

    this.get("/rest/getLicense*") { ctx ->
        success(ctx, LicenseView(License()))
    }

    this.get("/rest/getOpenSubsonicExtensions*") { ctx ->
        success(ctx, OpenSubsonicExtensionsPayload(emptyList()))
    }

    this.get("/rest/getMusicFolders*") { ctx ->
        getMusicFolders(ctx, scope, dao)
    }

    this.get("/rest/getMusicDirectory*") { ctx ->
        getMusicDirectory(ctx, scope, dao)
    }

    this.get("/rest/getArtists*") { ctx ->
        getArtists(ctx, scope, dao)
    }

    this.get("/rest/getArtist*") { ctx ->
        getArtist(ctx, scope, dao)
    }

    this.get("/rest/getAlbumList2*") { ctx ->
        getAlbumList2(ctx, scope, dao)
    }

    this.get("/rest/getAlbumList*") { ctx ->
        getAlbumList(ctx, scope, dao)
    }

    this.get("/rest/getAlbum*") { ctx ->
        getAlbum(ctx, scope, dao)
    }

    this.get("/rest/getSong*") { ctx ->
        getSong(ctx, scope, dao)
    }

    this.get("/rest/getIndexes*") { ctx ->
        getIndexes(ctx, scope, dao)
    }

    this.get("/rest/search3*") { ctx ->
        search3(ctx, scope, dao)
    }

    this.get("/rest/stream*") { ctx ->
        stream(ctx, scope, dao, context)
    }

    this.get("/rest/getCoverArt*") { ctx ->
        getCoverArt(ctx, scope, dao, context)
    }

    this.get("/rest/getPlaylists*") { ctx ->
        getPlaylists(ctx, scope, dao)
    }

    this.get("/rest/getPlaylist*") { ctx ->
        getPlaylist(ctx, scope, dao)
    }

    this.get("/rest/createPlaylist*") { ctx ->
        createPlaylist(ctx, scope, dao)
    }

    this.get("/rest/updatePlaylist*") { ctx ->
        updatePlaylist(ctx, scope, dao)
    }

    this.get("/rest/deletePlaylist*") { ctx ->
        deletePlaylist(ctx, scope, dao)
    }

    this.get("/rest/star*") { ctx ->
        star(ctx, scope, dao)
    }

    this.get("/rest/unstar*") { ctx ->
        unstar(ctx, scope, dao)
    }

    this.get("/rest/setRating*") { ctx ->
        setRating(ctx, scope, dao)
    }

    this.get("/rest/getStarred2*") { ctx ->
        getStarred2(ctx, scope, dao)
    }

    this.get("/rest/getStarred*") { ctx ->
        getStarred(ctx, scope, dao)
    }

    this.get("/rest/scrobble*") { ctx ->
        scrobble(ctx, scope, dao)
    }

    this.get("/rest/getNowPlaying*") { ctx ->
        getNowPlaying(ctx, scope, dao)
    }

    this.get("/rest/getUsers*") { ctx ->
        getUsers(ctx, scope, dao)
    }

    this.get("/rest/getUser*") { ctx ->
        getUser(ctx, scope, dao)
    }

    this.get("/rest/getGenres*") { ctx ->
        getGenres(ctx, scope, dao)
    }

    this.error(404) { ctx ->
        reject(ctx, Reject.NODATA)
        Log.i("Routes", "Client requested route: ${ctx.fullUrl()}")
    }

    this.after("/*") { ctx ->
        Log.i("Routes", "200; Client requested: ${ctx.fullUrl()}")
    }
}

fun Long.toIsoTime(): String? {
    return Instant.ofEpochSecond(this).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}
