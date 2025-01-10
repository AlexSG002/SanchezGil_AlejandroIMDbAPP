package com.pmdm.snchezgil_alejandroimdbapp.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class BuscarFragment extends Fragment {

    private FragmentBuscarBinding binding;
    private ExecutorService executorService;
    private Handler mainHandler;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "ce59811d13eeb643ae1389e360a103dd";
    private static final String ENDPOINT = "genre/movie/list?language=en";
    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZTU5ODExZDEzZWViNjQzYWUxMzg5ZTM2MGExMDNkZCIsIm5iZiI6MTczNjUwMjMyMy4yMTQwMDAyLCJzdWIiOiI2NzgwZWMzMzE0MzFlMDU5MWFiYjJmYzQiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.ykP4qXAyJ40Io_luvUthjnYOawrFEMu-cMULOzsTwoQ";
    private static final String ACCEPT = "application/json";
    private static List<Genero> generos = new ArrayList<Genero>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cargarGeneros();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void cargarGeneros(){
        executorService.execute(() ->{

                try {
                    String urlString = BASE_URL + ENDPOINT;
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