package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface ErrorReportDao {

    @Query("SELECT * FROM error_reports ORDER BY timestamp DESC")
    List<ErrorReport> getAllErrorReports();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertErrorReport(ErrorReport errorReport);

    @Query("SELECT * FROM error_reports WHERE id = :id LIMIT 1")
    ErrorReport getReportById(int id);


    @Query("DELETE FROM error_reports WHERE id = :id")
    void deleteErrorReport(int id);
}
