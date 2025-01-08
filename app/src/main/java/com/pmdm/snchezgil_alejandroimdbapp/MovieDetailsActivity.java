package com.pmdm.snchezgil_alejandroimdbapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pmdm.snchezgil_alejandroimdbapp.models.Movie;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView imagePosterLarge;
    private TextView textTitle, textRank, textPlot, textDate;
    private ExecutorService executorService;
    private Button buttonEnviar;
    private static final int CODIGO_PERMISO_LEER_CONTACTOS = 1;
    private static final int CODIGO_PERMISO_ENVIAR_SMS = 2;
    private ActivityResultLauncher<Intent> launcherSeleccionarContacto;

    private String numeroSMSPendiente;
    private String textoSMSPendiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        imagePosterLarge = findViewById(R.id.imagePosterLarge);
        textTitle = findViewById(R.id.textTitle);
        textRank = findViewById(R.id.textRank);
        textPlot = findViewById(R.id.textPlot);
        textDate = findViewById(R.id.textDate);
        buttonEnviar = findViewById(R.id.buttonEnviarSMS);

        launcherSeleccionarContacto = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            String idContacto = obtenerIdContacto(contactUri);
                            if (idContacto != null) {
                                String numTelefono = obtenerTelefono(idContacto);
                                if (numTelefono != null && !numTelefono.isEmpty()) {
                                    String textoSMS = "¡Te recomiendo la película: " + textTitle.getText().toString() + "!"+" Con rating: "+textRank.getText().toString();
                                    enviarSMS(numTelefono, textoSMS);
                                } else {
                                    Toast.makeText(this, "El contacto no tiene número de teléfono.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MovieDetailsActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MovieDetailsActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, CODIGO_PERMISO_LEER_CONTACTOS);
                }else{
                    lanzarSelectorContactos();
                }

            }
        });

        executorService = Executors.newSingleThreadExecutor();

        Movie movie = getIntent().getParcelableExtra("MOVIE");

        if (movie != null) {
            textTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Sin título");
            textRank.setText("Rank: " + movie.getRank());
            textPlot.setText("Descripción: "+movie.getDescripcion());
            textDate.setText("Fecha de lanzamiento: "+movie.getFecha());

            executorService.execute(() -> {
                try {
                    URL url = new URL(movie.getImageUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    runOnUiThread(() -> imagePosterLarge.setImageBitmap(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> imagePosterLarge.setImageResource(R.drawable.ic_launcher_foreground));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISO_LEER_CONTACTOS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lanzarSelectorContactos();
            } else {
                Toast.makeText(this, "Permiso de lectura de contactos denegado.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CODIGO_PERMISO_ENVIAR_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirAppSMS(numeroSMSPendiente, textoSMSPendiente);
            } else {
                Toast.makeText(this, "Permiso para enviar SMS denegado.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void lanzarSelectorContactos() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        launcherSeleccionarContacto.launch(intent);
    }

    private String obtenerIdContacto(Uri contactUri) {
        String idContacto = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            idContacto = cursor.getString(idIndex);
        }
        if (cursor != null) {
            cursor.close();
        }
        return idContacto;
    }

    private String obtenerTelefono(String contactId) {
        String numTelefono = null;
        Cursor cursorTelefono = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (cursorTelefono != null && cursorTelefono.moveToFirst()) {
            int numberIndex = cursorTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            numTelefono = cursorTelefono.getString(numberIndex);
        }
        if (cursorTelefono != null) {
            cursorTelefono.close();
        }
        return numTelefono;
    }

    private void enviarSMS(String numero, String texto) {
        numeroSMSPendiente = numero;
        textoSMSPendiente = texto;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    CODIGO_PERMISO_ENVIAR_SMS
            );
        } else {
            abrirAppSMS(numero, texto);
        }
    }

    private void abrirAppSMS(String numero, String texto) {
        if (numero == null || texto == null || numero.isEmpty() || texto.isEmpty()) {
            Toast.makeText(this, "No se tiene número o texto para enviar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + numero));
        smsIntent.putExtra("sms_body", texto);
        startActivity(smsIntent);
    }
}


