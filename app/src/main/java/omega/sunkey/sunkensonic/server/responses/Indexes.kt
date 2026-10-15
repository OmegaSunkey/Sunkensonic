package omega.sunkey.sunkensonic.server.responses

import com.fasterxml.jackson.annotation.JsonInclude

data class Indexes(
    val ignoredArticles: String,
    val index: List<Index>
)

data class Index(
    val name: String,
    @param:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val artist: List<ArtistData>,
    @param:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val child: List<AlbumData>
)

data class IndexesView(
    val indexes: Indexes
)