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

import com.pmdm.snchezgil_alejandroimdbapp.MainActivity;
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
    private FavoritesDatabaseHelper database;
    private boolean favoritos;
    public MovieAdapter(Context context, List<Movie> movies, String idUsuario, FavoritesDatabaseHelper database, boolean favoritos){
        this.context = context;
        this.movies = movies;
        this.idUsuario = idUsuario;
        this.database = database;
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
        FavoritesDatabaseHelper database = new FavoritesDatabaseHelper(context);
        SQLiteDatabase db = database.getWritableDatabase();
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
            if(!favoritos){
                database.insertarFavorito(db, idUsuario, movie.getId(),movie.getTitle(),movie.getDescripcion(), movie.getFecha(), movie.getRank(), movie.getImageUrl());
                Toast.makeText(context,"Se ha agregado a favoritos: "+movie.getTitle(),Toast.LENGTH_SHORT).show();
            }else{
                int filasBorradas = db.delete(FavoritesDatabaseHelper.TABLE_FAVORITOS, "idUsuario=? AND idPelicula=?",
                        new String[]{idUsuario, movie.getId()});
                if(filasBorradas>0){
                    Toast.makeText(context,"Se ha eliminado de favoritos: "+movie.getTitle(),Toast.LENGTH_SHORT).show();
                    movies.remove(position);
                    notifyItemRemoved(position);
                }
            }
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
}
