package edu.adam.artinstituteofchicago.gallerycomponents;

import java.io.Serializable;

/** One gallery from the /galleries endpoint.
 * Serializable so it can be passed to ArtworkListActivity with putExtra. */
public class Gallery implements Serializable {

    private final long id;
    private final String title;
    private final String number;
    private final String floor;
    private final boolean is_closed;
    private final String source_updated_at;

    // Not part of the galleries response: the first artwork returned for this gallery
    private String imageId = "";

    public Gallery(long id, String title, String number, String floor,
                   boolean is_closed, String lastUpdate) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.floor = floor;
        this.is_closed = is_closed;
        this.source_updated_at = lastUpdate;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getFloor() {
        return floor;
    }

    public boolean isIs_closed() {
        return is_closed;
    }

    public String getSource_updated_at() {
        return source_updated_at;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
