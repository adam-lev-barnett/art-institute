package edu.adam.artinstituteofchicago.gallerycomponents;

import java.io.Serializable;

/** One gallery from the /galleries endpoint.
 * Serializable so it can be passed to ArtworkListActivity with putExtra. */
public class Gallery implements Serializable {

    private final long id;
    private final String name;
    private final String number;
    private final String floor;
    private final boolean closed;
    private final String lastUpdate;

    // Not part of the galleries response: the first artwork returned for this gallery
    private String imageId = "";

    public Gallery(long id, String name, String number, String floor,
                   boolean closed, String lastUpdate) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.floor = floor;
        this.closed = closed;
        this.lastUpdate = lastUpdate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getFloor() {
        return floor;
    }

    public boolean isClosed() {
        return closed;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
