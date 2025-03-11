package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.slider.Slider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ErrorReportActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private EditText description;
    private Slider severitySlider;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private String imagePath;
    private static String address = "130.236.81.13";
    private static int port = 8718;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);

        imageView = findViewById(R.id.imageView);
        description = findViewById(R.id.editTextDescription);
        severitySlider = findViewById(R.id.sliderSeverity);
        Button captureButton = findViewById(R.id.buttonCapture);
        Button submitButton = findViewById(R.id.buttonSubmit);
        Button backButton = findViewById(R.id.buttonBack);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        captureButton.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });
        submitButton.setOnClickListener(v -> {
            try {
                submitReport();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        backButton.setOnClickListener(v -> finish());

        Button viewReportsButton = findViewById(R.id.buttonViewReports);
        viewReportsButton.setOnClickListener(v -> {
            Log.d("ErrorReportActivity", "Visa felrapporter-knappen tryckt");
            Intent intent = new Intent(ErrorReportActivity.this, ErrorReportListActivity.class);
            startActivity(intent);
        });

    }
    private File createImageFile() throws IOException {
        // Skapa ett unikt filnamn baserat på tidsstämpel
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Hämta katalogen för lagring av bilder
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Skapa filen
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Spara filens sökväg så vi kan använda den senare
        imagePath = imageFile.getAbsolutePath();
        return imageFile;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(this, "se.liu.itn.kts.tnk115.TNK115_Project_2025.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Läs in bilden från filen
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Skalar ner bilden för att minska minnesanvändning
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

                // Hämta EXIF-data
                ExifInterface exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Log.d("ErrorReportActivity", "EXIF Orientation: " + orientation);
                // Roterar bilden baserat på EXIF-rotation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);

                // Sätt bilden i ImageView
                imageBitmap = rotatedBitmap;
                imageView.setImageBitmap(imageBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap; // Om ingen rotation behövs, returnera originalbilden
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }



    private String saveImageToFile() {
        try {

            File outputDir = getCacheDir();
            File imageFile = File.createTempFile("error_image", ".jpg", outputDir);
            FileOutputStream fos = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            imagePath = imageFile.getAbsolutePath();

            Bitmap bm = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            baos.close();


            return android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);


        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
            }
        });
    }

    private void submitReport() throws JSONException {

        String desc = description.getText().toString().trim();
        int severity = (int) severitySlider.getValue();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (desc.isEmpty() || imageBitmap == null || currentLocation == null) {
            Toast.makeText(this, "Vänligen fyll i alla fält och ta en bild", Toast.LENGTH_SHORT).show();
            return;
        }
        String image64 = saveImageToFile();
        ErrorReport errorReport = new ErrorReport(desc, severity, timestamp, currentLocation.getLatitude(), currentLocation.getLongitude(), image64);
        MainActivity.errorReportDao.insertErrorReport(errorReport);

        Toast.makeText(this, "Felrapport inskickad!", Toast.LENGTH_SHORT).show();
        finish();


        JSONObject message = errorReport.getJson();
        Log.d("ErrorReportActivity", "Trying to send error report (PostGres): " + message.toString());
        //MainActivity.submitErrorReport(message);
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(TELEPHONY_SERVICE);

        StringBuilder builder = new StringBuilder();
        builder.append(message.toString());
        builder.append((char) '|');

        byte[] packetToSend = builder.toString().getBytes();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        JSONObject responseMessage = null;
        try {
            InetAddress serverAddrIP = InetAddress.getByName(address);
            Socket socket = new Socket(serverAddrIP, port);
            PrintStream exit = new PrintStream(socket.getOutputStream(), false);
            InputStream inputStream = socket.getInputStream();
            exit.write(packetToSend);
            Log.i("ErrorReportActivity", "Packet Sent ");
            exit.flush();

            ByteArrayOutputStream holder = new ByteArrayOutputStream();
            int outer_loop_limit = 0;

            while (socket.isConnected() && outer_loop_limit < 1) {
                char curr;
                curr = (char) inputStream.read();

                if (curr == '|') {
                    break;
                }

                holder.write(curr);
            }
            String response = holder.toString();

            response = response.trim();
            Log.d("ErrorReportActivity", "Response: " + response);
            if (response.startsWith("{") || response.startsWith("[")) {
                try {
                    if (response.startsWith("[")) {
                        response = response.substring(1, message.length() - 1);
                        responseMessage = new JSONObject(response);
                    } else {
                        responseMessage = new JSONObject(response);
                    }
                } catch (JSONException e) {
                    Log.w("ErrorReportActivity", "JSONException 1: " + e.toString());
                }
            }

            exit.close();
            socket.close();

        } catch (IOException e) {
            Log.w("ErrorReportActivity", "IOException: " + e.toString());
        }
        Log.d("ErrorReportActivity", "Update message sent!");
        Log.d("ErrorReportActivity", message.toString());
        Log.d("ErrorReportActivity", "Update message received");
        if (responseMessage == null) Log.w("UpdateActivity", "Response null");
        else Log.d("ErrorReportActivity", responseMessage.toString());

        /*try {
            if (responseMessage.has(input)) {
                result = responseMessage.getString(input);
            }
        } catch (JSONException e) {
            Log.w("UpdateActivity", "JSONException: " + e.toString());
        }*/
    }
}
