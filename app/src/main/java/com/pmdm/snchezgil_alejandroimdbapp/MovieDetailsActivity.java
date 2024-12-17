package com.pmdm.snchezgil_alejandroimdbapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView imagePosterLarge;
    private TextView textTitle, textRank, textPlot;
    private ExecutorService executorService;
    private Button buttonEnviar;
    private static final int CODIGO_PERMISO_LEER_CONTACTOS = 1;
    private static final int CODIGO_PERMISO_ENVIAR_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        imagePosterLarge = findViewById(R.id.imagePosterLarge);
        textTitle = findViewById(R.id.textTitle);
        textRank = findViewById(R.id.textRank);
        textPlot = findViewById(R.id.textPlot);
        buttonEnviar = findViewById(R.id.buttonEnviarSMS);

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (ContextCompat.checkSelfPermission(MovieDetailsActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MovieDetailsActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, CODIGO_PERMISO_LEER_CONTACTOS);
                }

                if (ContextCompat.checkSelfPermission(MovieDetailsActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MovieDetailsActivity.this, new String[]{Manifest.permission.SEND_SMS}, CODIGO_PERMISO_ENVIAR_SMS);
                }
                */
            }
        });

        executorService = Executors.newSingleThreadExecutor();

        Movie movie = getIntent().getParcelableExtra("MOVIE");

        if (movie != null) {
            textTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Sin título");
            textRank.setText("Rank: " + movie.getRank());
            textPlot.setText("Descripción: "+movie.getDescripcion());

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
