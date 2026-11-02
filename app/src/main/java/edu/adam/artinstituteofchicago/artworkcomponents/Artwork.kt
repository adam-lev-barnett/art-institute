package edu.adam.artinstituteofchicago.artworkcomponents

import java.io.Serializable

/** One artwork from the /artworks/search endpoint.
 * Serializable so it can be passed to ArtworkDetailActivity with putExtra.  */
class Artwork(
    val id: Long, val title: String?, val artistDisplay: String?, val dateDisplay: String?,
    val mediumDisplay: String?, val placeOfOrigin: String?, val imageId: String?,
    val dimensions: String?, // Included in the spec but unused on any of the actual views
    var shortDescription: String?, description: String?,
    creditLine: String?, galleryTitle: String?
) : Serializable {
    val description: String?

    // Included in the spec but unused on any of the actual views
    val creditLine: String?
    val galleryTitle: String?

    init {
        this.shortDescription = shortDescription
        this.description = description
        this.creditLine = creditLine
        this.galleryTitle = galleryTitle
    }
}
