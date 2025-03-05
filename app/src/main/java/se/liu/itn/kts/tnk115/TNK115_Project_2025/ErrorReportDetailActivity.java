package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import android.graphics.Matrix;

public class ErrorReportDetailActivity extends AppCompatActivity {

    private TextView textDescription, textSeverity, textTimestamp, textLocation;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report_detail);

        textDescription = findViewById(R.id.textDescription);
        textSeverity = findViewById(R.id.textSeverity);
        textTimestamp = findViewById(R.id.textTimestamp);
        textLocation = findViewById(R.id.textLocation);
        imageView = findViewById(R.id.imageViewDetail);

        int reportId = getIntent().getIntExtra("report_id", -1);
        if (reportId != -1) {
            loadErrorReport(reportId);
        }
    }

    private void loadErrorReport(int reportId) {
        ErrorReport report = MainActivity.errorReportDao.getReportById(reportId);
        if (report != null) {
            textDescription.setText(report.description);
            textSeverity.setText("Allvarlighet: " + report.severity);
            textTimestamp.setText("Rapporterad: " + report.timestamp);
            textLocation.setText("Plats: " + report.latitude + ", " + report.longitude);

            // Ladda och rotera bilden om den finns
            if (report.imagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(report.imagePath);
                bitmap = rotateBitmapIfNeeded(bitmap, report.imagePath);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

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
                    return bitmap; // Ingen rotation behövs
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap; // Om vi inte kan läsa EXIF, returnera originalbilden
        }
    }

}
