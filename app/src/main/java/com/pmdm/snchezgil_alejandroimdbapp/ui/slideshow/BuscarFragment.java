package com.pmdm.snchezgil_alejandroimdbapp.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pmdm.snchezgil_alejandroimdbapp.R;
import com.pmdm.snchezgil_alejandroimdbapp.databinding.FragmentBuscarBinding;
import com.pmdm.snchezgil_alejandroimdbapp.models.Genero;
import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuscarFragment extends Fragment {

    private FragmentBuscarBinding binding;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String ENDPOINT_GENEROS = "genre/movie/list?language=en";
    private static String ENDPOINT_PELIS = "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&primary_release_year=";
    private static String urlGenero = "&with_genres=";
    private static String urlSortDesc = "&sort_by=popularity.desc";
    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZTU5ODExZDEzZWViNjQzYWUxMzg5ZTM2MGExMDNkZCIsIm5iZiI6MTczNjUwMjMyMy4yMTQwMDAyLCJzdWIiOiI2NzgwZWMzMzE0MzFlMDU5MWFiYjJmYzQiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.ykP4qXAyJ40Io_luvUthjnYOawrFEMu-cMULOzsTwoQ";
    private static final String ACCEPT = "application/json";
    private EditText year;
    private Button buttonBuscar;
    private static List<Genero> generos = new ArrayList<Genero>();
    private Spinner spinnerGeneros;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        spinnerGeneros = binding.spinnerGeneros;
        year = binding.editTextNumber;
        buttonBuscar = binding.buttonBuscar;

        cargarGeneros();

        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yearText = year.getText().toString().trim();
                String generoSeleccionado = spinnerGeneros.getSelectedItem().toString();
                int idGeneroSeleccionado = 0;
                for(Genero genero : generos){
                    if(genero.getNombre().equals(generoSeleccionado)){
                        idGeneroSeleccionado = genero.getId();
                        break;
                    }
                }
                if(yearText.isEmpty()){
                    Toast.makeText(getContext(), "Ingrese un año válido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = ENDPOINT_PELIS+yearText+urlSortDesc+urlGenero+idGeneroSeleccionado;
                cargarPeliculas(url);
            }
        });



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
        binding = null;
    }

    private void cargarGeneros(){
        executorService.execute(() ->{
                try {
                    String urlString = BASE_URL + ENDPOINT_GENEROS;
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", AUTHORIZATION);
                    conn.setRequestProperty("accept", ACCEPT);
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
                        JsonArray genresArray = jsonObject.getAsJsonArray("genres");
                        for(JsonElement genreElement: genresArray){
                            JsonObject jsonGenre = genreElement.getAsJsonObject();

                            int idGenero = jsonGenre.get("id").getAsInt();
                            String nombreGenero = jsonGenre.get("name").getAsString();

                            Genero g = new Genero(idGenero, nombreGenero);
                            generos.add(g);
                        }

                        List<String> nombresGeneros = new ArrayList<>();
                        for(Genero genero : generos){
                            nombresGeneros.add(genero.getNombre());
                        }

                        mainHandler.post(()->{
                           if(!nombresGeneros.isEmpty()){
                               ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                       requireContext(),
                                       android.R.layout.simple_spinner_item,
                                       nombresGeneros
                               );
                               adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                               spinnerGeneros.setAdapter(adapter);
                           }else{
                               Toast.makeText(getContext(), "No se encontraron géneros.", Toast.LENGTH_SHORT).show();
                           }
                        });

                    }


                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        });
    }

    private void cargarPeliculas(String urlPeliculas){
        executorService.execute(() ->{
            try {
                String urlString = BASE_URL + urlPeliculas;
                Log.d("URL",urlString);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", AUTHORIZATION);
                conn.setRequestProperty("accept", ACCEPT);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(10000);
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = convertStreamToString(conn.getInputStream());
                    conn.disconnect();

                    Log.d("BuscarFragment", "Response: " + response);

                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                    JsonArray resultsArray = jsonObject.getAsJsonArray("results");

                    List<Movie> peliculasFiltradas = new ArrayList<>();

                    for(JsonElement resultsElement : resultsArray){
                        JsonObject jsonResultado = resultsElement.getAsJsonObject();
                        String title = jsonResultado.get("title").getAsString();
                        String id = jsonResultado.get("id").getAsString();
                        String overview = jsonResultado.get("overview").getAsString();
                        String vote_average = jsonResultado.get("vote_average").getAsString();
                        String releaseDate = jsonResultado.get("release_date").getAsString();
                        String imageURL = "https://image.tmdb.org/t/p/w600_and_h900_bestv2" + jsonResultado.get("poster_path").getAsString();

                        Movie m = new Movie();

                        m.setId(id);
                        m.setTitle(title);
                        m.setDescripcion(overview);
                        m.setRank("Fuera del top 10");
                        m.setRating(vote_average);
                        m.setFecha(releaseDate);
                        m.setImageUrl(imageURL);

                        peliculasFiltradas.add(m);
                    }

                    mainHandler.post(() ->{
                        if(!peliculasFiltradas.isEmpty()){

                            ArrayList<Movie> peliculasArrayList = new ArrayList<>(peliculasFiltradas);

                            Bundle bundle = new Bundle();

                            bundle.putParcelableArrayList("peliculas", peliculasArrayList);

                            NavController navController = NavHostFragment.findNavController(this);
                            navController.navigate(R.id.action_buscarFragment_to_pelisBuscadas, bundle);
                        }else{
                            Toast.makeText(getContext(), "No se encontraron películas", Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
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

}