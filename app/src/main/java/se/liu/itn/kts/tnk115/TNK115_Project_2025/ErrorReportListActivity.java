package se.liu.itn.kts.tnk115.TNK115_Project_2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ErrorReportListActivity extends AppCompatActivity {

    private ListView listView;
    private List<ErrorReport> errorReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report_list);

        listView = findViewById(R.id.listViewReports);
        Log.d("ErrorReportListActivity", "Laddar felrapporter...");
        loadErrorReports();
        Log.d("ErrorReportListActivity", "Felrapporter laddades.");
    }

    private void loadErrorReports() {
        Log.d("ErrorReportListActivity", "Laddar felrapporter...");
        errorReports = MainActivity.errorReportDao.getAllErrorReports();
        Log.d("ErrorReportListActivity", "Felrapporter laddades.");
        if (errorReports == null || errorReports.isEmpty()) {
            Log.d("ErrorReportListActivity", "Inga felrapporter hittades.");
            return;
        }

        ArrayAdapter<ErrorReport> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, errorReports);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            ErrorReport selectedReport = errorReports.get(position);
            Intent intent = new Intent(ErrorReportListActivity.this, ErrorReportDetailActivity.class);
            intent.putExtra("report_id", selectedReport.id);
            startActivity(intent);
        });
    }
}
