package com.pmdm.snchezgil_alejandroimdbapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView imagePosterLarge;
    private TextView textTitle, textRank;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        imagePosterLarge = findViewById(R.id.imagePosterLarge);
        textTitle = findViewById(R.id.textTitle);
        textRank = findViewById(R.id.textRank);

        executorService = Executors.newSingleThreadExecutor();

        Movie movie = getIntent().getParcelableExtra("MOVIE");

        if (movie != null) {
            textTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Sin título");
            textRank.setText("Rank: " + movie.getRank());

            executorService.execute(() -> {
                try {
                    URL url = new URL(movie.getImageUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    runOnUiThread(() -> imagePosterLarge.setImageBitmap(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> imagePosterLarge.setImageResource(R.drawable.ic_launcher_foreground));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
