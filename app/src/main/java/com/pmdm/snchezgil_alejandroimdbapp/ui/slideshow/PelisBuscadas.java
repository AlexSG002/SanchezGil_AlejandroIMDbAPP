package com.pmdm.snchezgil_alejandroimdbapp.ui.slideshow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.pmdm.snchezgil_alejandroimdbapp.R;
import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.util.ArrayList;
import java.util.List;


public class PelisBuscadas extends Fragment {

    private String idUsuario = FirebaseAuth. getInstance().getCurrentUser().getUid();
    private static List<Movie> pelis = new ArrayList<>();
    private boolean favoritos = false;

    public PelisBuscadas() {
    }

    public static PelisBuscadas newInstance(List<Movie>pelisFiltradas) {
        PelisBuscadas fragment = new PelisBuscadas();
        Bundle args = new Bundle();
        pelis = pelisFiltradas;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pelis_buscadas, container, false);
    }

    public void cargarPeliculas(){
        /*
        mainHandler.post(() -> {
                        if (finalMovies != null && !finalMovies.isEmpty()) {
                            MovieAdapter adapter = new MovieAdapter(getContext(), finalMovies, idUsuario, database, favoritos);
                            binding.recyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(getContext(), "No se pudieron obtener las películas", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    conn.disconnect();
                    mainHandler.post(() ->
                            Toast.makeText(getContext(), "Error en la petición: " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }
         */
    }

}