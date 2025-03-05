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
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        submitButton.setOnClickListener(v -> submitReport());
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



    private void saveImageToFile() {
        try {
            File outputDir = getCacheDir();
            File imageFile = File.createTempFile("error_image", ".jpg", outputDir);
            FileOutputStream fos = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            imagePath = imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
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

    private void submitReport() {
        String desc = description.getText().toString().trim();
        int severity = (int) severitySlider.getValue();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (desc.isEmpty() || imageBitmap == null || currentLocation == null) {
            Toast.makeText(this, "Vänligen fyll i alla fält och ta en bild", Toast.LENGTH_SHORT).show();
            return;
        }

        ErrorReport errorReport = new ErrorReport(desc, severity, timestamp, currentLocation.getLatitude(), currentLocation.getLongitude(), imagePath);
        MainActivity.errorReportDao.insertErrorReport(errorReport);

        Toast.makeText(this, "Felrapport inskickad!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
