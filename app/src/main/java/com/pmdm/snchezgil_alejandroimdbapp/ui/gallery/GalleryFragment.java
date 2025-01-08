package com.pmdm.snchezgil_alejandroimdbapp.ui.gallery;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.pmdm.snchezgil_alejandroimdbapp.adapter.MovieAdapter;
import com.pmdm.snchezgil_alejandroimdbapp.database.FavoritesDatabaseHelper;
import com.pmdm.snchezgil_alejandroimdbapp.databinding.FragmentGalleryBinding;
import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private ExecutorService executorService;
    private Handler mainHandler;

    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerViewFavoritos.setLayoutManager(layoutManager);

        cargarFavoritosDesdeBD();

        return root;
    }

    private void cargarFavoritosDesdeBD() {
        executorService.execute(() -> {
            FavoritesDatabaseHelper database = new FavoritesDatabaseHelper(requireContext());
            SQLiteDatabase db = database.getReadableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + FavoritesDatabaseHelper.TABLE_FAVORITOS + " WHERE idUsuario=?",
                    new String[]{userId}
            );

            List<Movie> pelisFavoritas = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int colIdPelicula = cursor.getColumnIndex("idPelicula");
                    int colTitulo = cursor.getColumnIndex("nombrePelicula");
                    int colDescripcion = cursor.getColumnIndex("descripcionPelicula");
                    int colFecha = cursor.getColumnIndex("fechaLanzamiento");
                    int colRanking = cursor.getColumnIndex("rankingPelicula");
                    int colCaratula = cursor.getColumnIndex("caratulaURL");

                    String idPelicula = cursor.getString(colIdPelicula);
                    String titulo = cursor.getString(colTitulo);
                    String descripcion = cursor.getString(colDescripcion);
                    String fecha = cursor.getString(colFecha);
                    int ranking = cursor.getInt(colRanking);
                    String caratula = cursor.getString(colCaratula);

                    Movie movie = new Movie();
                    movie.setId(String.valueOf(idPelicula));
                    movie.setTitle(titulo);
                    movie.setDescripcion(descripcion);
                    movie.setFecha(fecha);
                    movie.setRank(ranking);
                    movie.setImageUrl(caratula);

                    pelisFavoritas.add(movie);
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }
            db.close();

            mainHandler.post(() -> {
                if (!pelisFavoritas.isEmpty()) {
                    MovieAdapter adapter = new MovieAdapter(getContext(), pelisFavoritas, userId, database);
                    binding.recyclerViewFavoritos.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No tienes películas favoritas guardadas", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
        binding = null;
    }
}
