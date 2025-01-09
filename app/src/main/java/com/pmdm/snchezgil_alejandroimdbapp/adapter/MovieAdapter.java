package com.pmdm.snchezgil_alejandroimdbapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmdm.snchezgil_alejandroimdbapp.MovieDetailsActivity;
import com.pmdm.snchezgil_alejandroimdbapp.R;
import com.pmdm.snchezgil_alejandroimdbapp.database.FavoritesDatabaseHelper;
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
    private String idUsuario;
    private FavoritesDatabaseHelper databaseHelper;
    private boolean favoritos;

    public MovieAdapter(Context context, List<Movie> movies, String idUsuario, FavoritesDatabaseHelper databaseHelper, boolean favoritos){
        this.context = context;
        this.movies = movies;
        this.idUsuario = idUsuario;
        this.databaseHelper = databaseHelper;
        this.favoritos = favoritos;

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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("MOVIE", movie);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            executorService.execute(() -> {
                if (!favoritos) {
                    agregarFavorito(movie, holder.getAdapterPosition());
                } else {
                    eliminarFavorito(movie, holder.getAdapterPosition());
                }
            });

            return true;
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

    private void agregarFavorito(Movie movie, int position) {
        SQLiteDatabase dbWrite = databaseHelper.getWritableDatabase();
        long result = databaseHelper.insertarFavorito(dbWrite, idUsuario, movie.getId(), movie.getTitle(), movie.getDescripcion(), movie.getFecha(), movie.getRank(), movie.getImageUrl());
        dbWrite.close();

        if (result != -1) {
            mainHandler.post(() ->
                    Toast.makeText(context, "Se ha agregado a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show()
            );
        } else {
            mainHandler.post(() ->
                    Toast.makeText(context, "Error al agregar a favoritos.", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void eliminarFavorito(Movie movie, int position) {
        SQLiteDatabase dbWrite = databaseHelper.getWritableDatabase();
        int rowsDeleted = dbWrite.delete(
                FavoritesDatabaseHelper.TABLE_FAVORITOS,
                "idUsuario=? AND idPelicula=?",
                new String[]{idUsuario, movie.getId()}
        );
        dbWrite.close();

        if (rowsDeleted > 0) {
            mainHandler.post(() -> {
                Toast.makeText(context, "Eliminado de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                movies.remove(position);
                notifyItemRemoved(position);
            });
        } else {
            mainHandler.post(() ->
                    Toast.makeText(context, "Error al eliminar: " + movie.getTitle(), Toast.LENGTH_SHORT).show()
            );
        }
    }
}
