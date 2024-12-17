package com.pmdm.snchezgil_alejandroimdbapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pmdm.snchezgil_alejandroimdbapp.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    //Declaramos variables
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;
    private TextView nombre;
    private TextView email;
    private ImageView imagen;
    private ExecutorService executorService;
    private Handler mainHandler;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser usuario = mAuth.getCurrentUser();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);

        Button LogoutButton = headerView.findViewById(R.id.buttonLogout);
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        volverALogin();
                    }
                });
            }
        });

        nombre = headerView.findViewById(R.id.nombre);
        email = headerView.findViewById(R.id.email);
        imagen = headerView.findViewById(R.id.imageView);

        if(usuario != null) {
            String nombreCuenta = usuario.getDisplayName();
            String emailCuenta = usuario.getEmail();
            Uri imagenCuenta = usuario.getPhotoUrl();

            nombre.setText(nombreCuenta);
            email.setText(emailCuenta);

            if(imagenCuenta!=null){
                executorService.execute(new DescargarImagen(imagenCuenta.toString(), imagen));
            }

        }else{
            volverALogin();
        }
    }

    private void volverALogin(){
        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class DescargarImagen implements Runnable {
        private final String url;
        private final ImageView imageView;

        private DescargarImagen(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        public void run() {
            try {
                byte[] imagenBytes = descargaImagen(url);

                BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);

                mainHandler.post(() -> imageView.setImageBitmap(bitmap));

            } catch (IOException e) {
                mainHandler.post(() -> Toast.makeText(MainActivity.this,
                        "Error al descargar la imagen: URL incorrecta (https://www.ejemplo.png/jpg/jpeg)",
                        Toast.LENGTH_SHORT).show());
            }
        }

        private byte[] descargaImagen(String myurl) throws IOException {
            InputStream is = null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                int response = conn.getResponseCode();
                if (response != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Error en la conexión: " + response);
                }
                is = conn.getInputStream();
                byte[] datos = new byte[1024];
                int nRead;

                while ((nRead = is.read(datos, 0, datos.length)) != -1) {
                    buffer.write(datos, 0, nRead);
                }

                return buffer.toByteArray();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if(usuarioActual == null){
            volverALogin();
        }
    }
}
