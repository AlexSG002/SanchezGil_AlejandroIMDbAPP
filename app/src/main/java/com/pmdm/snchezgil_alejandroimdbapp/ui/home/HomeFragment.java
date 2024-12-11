package com.pmdm.snchezgil_alejandroimdbapp.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pmdm.snchezgil_alejandroimdbapp.adapter.MovieAdapter;
import com.pmdm.snchezgil_alejandroimdbapp.databinding.FragmentHomeBinding;
import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ExecutorService executorService;
    private Handler mainHandler;

    private static final String BASE_URL = "https://imdb-com.p.rapidapi.com/";
    private static final String API_KEY = "200ca2873dmsh3c28ce355613a89p1dd78cjsndb8f2f9c0b09";
    private static final String HOST = "imdb-com.p.rapidapi.com";
    private static final String ENDPOINT = "title/get-top-meter?topMeterTitlesType=ALL";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Usamos un GridLayoutManager con 2 columnas
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerView.setLayoutManager(layoutManager);

        cargarTopMovies();
        return root;
    }

    private void cargarTopMovies() {
        executorService.execute(() -> {
            try {
                String urlString = BASE_URL + ENDPOINT;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("x-rapidapi-key", API_KEY);
                conn.setRequestProperty("x-rapidapi-host", HOST);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(10000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = convertStreamToString(conn.getInputStream());
                    conn.disconnect();

                    Log.d("HomeFragment", "Response: " + response);

                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                    JsonObject dataObject = jsonObject.getAsJsonObject("data");
                    JsonObject topMeterTitlesObject = dataObject.getAsJsonObject("topMeterTitles");
                    JsonArray edgesArray = topMeterTitlesObject.getAsJsonArray("edges");

                    List<Movie> movies = new ArrayList<>();

                    for (JsonElement edgeElement : edgesArray) {
                        JsonObject edgeObject = edgeElement.getAsJsonObject();
                        JsonObject nodeObject = edgeObject.getAsJsonObject("node");

                        // Solo necesitamos la imagen para la carátula
                        String imageUrl = nodeObject.getAsJsonObject("primaryImage").get("url").getAsString();

                        Movie movie = new Movie();
                        movie.setImageUrl(imageUrl);
                        movies.add(movie);
                    }

                    // Tomar sólo top 10 si hay más
                    if (movies.size() > 10) {
                        movies = movies.subList(0, 10);
                    }

                    final List<Movie> finalMovies = movies;
                    mainHandler.post(() -> {
                        if (finalMovies != null && !finalMovies.isEmpty()) {
                            MovieAdapter adapter = new MovieAdapter(getContext(), finalMovies);
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

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Error al cargar las películas: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
        binding = null;
    }
}
