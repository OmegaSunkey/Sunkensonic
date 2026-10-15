package omega.sunkey.sunkensonic.server.responses

import com.fasterxml.jackson.annotation.JsonInclude

data class Artists(
    val ignoredArticles: String,
    val index: List<ArtistIndex>
)

data class ArtistIndex(
    val name: String, //this is by index: A B C .. Z #
    val artist: List<ArtistData>
)

data class ArtistData(
    val id: String,
    val name: String,
    val albumCount: Int,
    val coverArt: String,
    val userRating: Int = 0,
    val artistImageUrl: String? = null,
    val playCount: Int = 0,
    val played: String? = null,
    val starred: String? = null,
    @param:JsonInclude(JsonInclude.Include.NON_NULL)
    val album: List<AlbumData>? = null
)

data class ArtistsView(
    val artists: Artists
)

data class SingleArtistView(
    val artist: ArtistData
)