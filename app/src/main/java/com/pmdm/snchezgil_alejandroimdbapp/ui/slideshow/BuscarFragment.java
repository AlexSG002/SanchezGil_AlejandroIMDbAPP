package com.pmdm.snchezgil_alejandroimdbapp.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pmdm.snchezgil_alejandroimdbapp.databinding.FragmentBuscarBinding;
import com.pmdm.snchezgil_alejandroimdbapp.models.Genero;

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
    private static final String ENDPOINT_PELIS = "search/movie?include_adult=false&language=en-US&page=1";
    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZTU5ODExZDEzZWViNjQzYWUxMzg5ZTM2MGExMDNkZCIsIm5iZiI6MTczNjUwMjMyMy4yMTQwMDAyLCJzdWIiOiI2NzgwZWMzMzE0MzFlMDU5MWFiYjJmYzQiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.ykP4qXAyJ40Io_luvUthjnYOawrFEMu-cMULOzsTwoQ";
    private static final String ACCEPT = "application/json";
    private static List<Genero> generos = new ArrayList<Genero>();
    private Spinner spinnerGeneros;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        spinnerGeneros = binding.spinnerGeneros;

        cargarGeneros();

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

    private void cargarPeliculas(){
        executorService.execute(() ->{
            try {
                String urlString = BASE_URL + ENDPOINT_PELIS;
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