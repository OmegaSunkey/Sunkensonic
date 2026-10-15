package omega.sunkey.sunkensonic.server.endpoints

import io.javalin.http.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import omega.sunkey.sunkensonic.server.responses.Genre
import omega.sunkey.sunkensonic.server.responses.Genres
import omega.sunkey.sunkensonic.server.responses.GenresView
import omega.sunkey.sunkensonic.server.room.SubsonicDao
import omega.sunkey.sunkensonic.server.success

fun getGenres(context: Context, scope: CoroutineScope, dao: SubsonicDao) {
    context.future(scope.future {
        val allSongs = dao.getAllSongs()
        val genreList = allSongs.groupBy { it.genre }
        val genres = mutableListOf<Genre>()

        for (genre in genreList) {
            val albumList = genre.value.groupBy { it.albumId }
            genres.add(Genre(
                genre.value.size,
                albumList.size,
                genre.key ?: "Unknown Genre"
            ))
        }
        success(context, GenresView(Genres(genres)))
    })
}