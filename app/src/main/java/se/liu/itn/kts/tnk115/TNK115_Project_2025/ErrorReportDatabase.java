package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ErrorReport.class}, version = 1)
public abstract class ErrorReportDatabase extends RoomDatabase {
    public abstract ErrorReportDao errorReportDao();

    private static volatile ErrorReportDatabase INSTANCE;

    public static ErrorReportDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ErrorReportDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ErrorReportDatabase.class,
                            "error_reports_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
