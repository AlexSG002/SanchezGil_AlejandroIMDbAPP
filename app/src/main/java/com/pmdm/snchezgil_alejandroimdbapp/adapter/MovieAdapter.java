package com.pmdm.snchezgil_alejandroimdbapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmdm.snchezgil_alejandroimdbapp.MovieDetailsActivity;
import com.pmdm.snchezgil_alejandroimdbapp.R;
import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    private Context context;
    private List<Movie> movies;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler();

    public MovieAdapter(Context context, List<Movie> movies){
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Movie movie = movies.get(position);

        // Cargar la imagen en segundo plano
        executorService.execute(() -> {
            try {
                URL url = new URL(movie.getImageUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                mainHandler.post(() -> holder.posterImageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> holder.posterImageView.setImageResource(R.drawable.ic_launcher_foreground));
            }
        });

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("MOVIE", movie); // Movie es parcelable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return movies != null ? movies.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
        }
    }
}
