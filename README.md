# Art Institute of Chicago

An Android app for browsing the galleries and artwork of the Art Institute of Chicago

All gallery and artwork content is pulled live from the
[Art Institute of Chicago API](https://api.artic.edu/docs).

**Author:** Adam Barnett

## Features

### Base app
- **Home (`MainActivity`)** – Cards for Browse by Gallery, Search Artworks, and Random Artwork.
- **Galleries (`GalleryActivity`)** – Every gallery in the museum, with its name, floor, and a
  thumbnail taken from the first artwork the API returns for that gallery. Galleries with no floor
  leave the floor blank.
- **Artworks (`ArtworkListActivity`)** – All artworks in the selected gallery, with the gallery
  name, floor, artwork count, and the API's "Last update" date.
- **Artwork Detail (`ArtworkDetailActivity`)** – Zoomable image (double-tap or pinch), followed by
  the title, artist and date, gallery, medium, dimensions, location, and description in a
  ScrollView. Fields the API doesn't provide are hidden along with their labels.

### Additional functionality (all three completed)
- **Search (`SearchActivity`)** – Keyword search across the collection. Shows a "No Results"
  dialog when nothing matches.
- **Favorites (`FavoritesActivity`)** – Tap the heart on the detail screen to favorite an
  artwork. Favorites are sorted by title and saved to internal storage with Gson, so they persist
  between launches. Long-press an entry to remove it (with a confirmation dialog).
- **Random Artwork** – Opens the detail screen with a randomly chosen public-domain artwork that
  has an image.

### Throughout the app
- Bottom navigation bar on every screen. The current screen's icon is red and the others are
  grey.
- A separate landscape layout for every activity (`res/layout-land`).
- Error conditions such as no network connection, failed downloads, and unreadable favorites
  show an AlertDialog instead of failing silently.
- Rotating the device keeps the current search results and the current random artwork.

## Requirements

| | |
|---|---|
| Compile / Target SDK | 37 |
| Minimum SDK | 33 |
| Language | Java (XML layouts, View Binding) |
| Tested on | Pixel 10 and Pixel 7a emulators, API 37.0 |

## Running the app

1. Open the project folder in Android Studio.
2. Let Gradle sync. Dependencies come from Google, Maven Central, and JitPack (for PhotoView).
3. Run the `app` configuration on an emulator or device with internet access.

No API key is needed, because the Art Institute API is public. The app only needs the
`INTERNET` permission.

## Libraries

| Library | Used for |
|---|---|
| Volley | Making the API requests |
| Glide | Loading artwork images, with the User-Agent headers the image server requires |
| PhotoView | The zoomable image on the detail screen |
| Gson | Saving and loading favorites as JSON |
| RecyclerView / CardView | The gallery, artwork, search, and favorites lists |

## API notes

- **Galleries:** `GET /api/v1/galleries`, 100 per page, following each page until none remain.
- **Artworks in a gallery:** `GET /api/v1/artworks/search?query[term][gallery_id]=…`, also
  fetched page by page.
- **Search:** `GET /api/v1/artworks/search?q=…`. Each keyword is wrapped in quotes before it's
  sent, because unquoted terms sometimes made the API return unrelated results.
- **Random artwork:** `POST /api/v1/artworks/search` with a `function_score` / `random_score`
  query, seeded with the current time as the API docs recommend.
- **Images:** loaded from the IIIF server at 1200px. If that fails the app retries at 800px, and
  if that also fails it shows the provided default image. Thumbnails use 400px.
- The `galleryId` field from the spec comes back empty from the API, so the app also requests
  `gallery_title` to show the gallery name on the detail screen.

## Project structure

```
app/src/main/java/edu/adam/artinstituteofchicago/
├── MainActivity, GalleryActivity, ArtworkListActivity,
│   ArtworkDetailActivity, SearchActivity, FavoritesActivity
├── artworkcomponents/   Artwork model, adapter, view holder, downloader, UserFavorites
├── gallerycomponents/   Gallery model, adapter, view holder, downloader
└── uicomponents/        BottomNavigation setup, ErrorDialog helper
```
