package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "error_reports")
public class ErrorReport {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "severity")
    public int severity;

    @ColumnInfo(name = "timestamp")
    public String timestamp;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    public ErrorReport(String description, int severity, String timestamp, double latitude, double longitude, String imagePath) {
        this.description = description;
        this.severity = severity;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Felrapport " + timestamp;
    }
}
