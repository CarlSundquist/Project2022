package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject getJson() throws JSONException {
        JSONObject message = new JSONObject();
        message.put("message_type", "error_report");
        message.put("id", 69);
        message.put("description", description);
        message.put("severity", severity);
        message.put("timestamp", timestamp);
        message.put("latitude", latitude);
        message.put("longitude", longitude);
        message.put("imagepath", imagePath);

        return message;
    }
}
