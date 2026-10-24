package edu.adam.artinstituteofchicago;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
    RecyclerView from API call has GALLERY objects, not artwork
    - Name, floor, image
    -- Floor is blank if no floor returned
    -- Image is FIRST returned artwork

    Tapping gallery opens Artworks

    Nav on bottom:
    - home, browse galleries, search artwork,
    - view faves, view random artwork
    -- Galleries icon should be red because currently on it

*/
public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}