package edu.adam.artinstituteofchicago.gallerycomponents

import java.io.Serializable

/** One gallery from the /galleries endpoint.
 * Serializable so it can be passed to ArtworkListActivity with putExtra.  */
class Gallery(
    val id: Long,
    val title: String?,
    val number: String?,
    val floor: String?,
    val updatedAt: String?
) : Serializable {
    // first artwork returned for this gallery
    var imageId: String? = ""

    // true once the thumbnail request has been sent, so rebinding the row doesn't send it again
    var isThumbnailFetched: Boolean = false
}
