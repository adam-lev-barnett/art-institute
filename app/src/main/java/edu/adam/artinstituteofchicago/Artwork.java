package edu.adam.artinstituteofchicago;

import java.io.Serializable;

/** One artwork from the /artworks/search endpoint.
 * Serializable so it can be passed to ArtworkDetailActivity with putExtra. */
public class Artwork implements Serializable {

    private final long id;
    private final String title;
    private final String artistDisplay;
    private final String dateDisplay;
    private final String mediumDisplay;
    private final String placeOfOrigin;
    private final String imageId;
    private final String dimensions;
    private final String shortDescription;
    private final String description;
    private final String creditLine;

    public Artwork(long id, String title, String artistDisplay, String dateDisplay,
                   String mediumDisplay, String placeOfOrigin, String imageId,
                   String dimensions, String shortDescription, String description,
                   String creditLine) {
        this.id = id;
        this.title = title;
        this.artistDisplay = artistDisplay;
        this.dateDisplay = dateDisplay;
        this.mediumDisplay = mediumDisplay;
        this.placeOfOrigin = placeOfOrigin;
        this.imageId = imageId;
        this.dimensions = dimensions;
        this.shortDescription = shortDescription;
        this.description = description;
        this.creditLine = creditLine;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistDisplay() {
        return artistDisplay;
    }

    public String getDateDisplay() {
        return dateDisplay;
    }

    public String getMediumDisplay() {
        return mediumDisplay;
    }

    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    public String getImageId() {
        return imageId;
    }

    public String getDimensions() {
        return dimensions;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getCreditLine() {
        return creditLine;
    }
}
