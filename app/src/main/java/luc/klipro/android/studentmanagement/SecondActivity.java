package luc.klipro.android.studentmanagement;

/**
 * Created by lucianahaugen on 15/05/17.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.EXTRA_USER;
import static android.nfc.NfcAdapter.EXTRA_ID;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class SecondActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;

    public void setHamtaIDString(String hamtaIDString) {
        this.hamtaIDString = hamtaIDString;
    }

    public String getHamtaIDString() {
        return hamtaIDString;
    }

    // URL för specifik sudentID
    // Måste spara studentID på mainA och sedan skicka hit.
    private String hamtaIDString;
    private String url = "http://localhost:8080/students?format=json&id=";

    ArrayList<HashMap<String, String>> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        setHamtaIDString(intent.getStringExtra(EXTRA_MESSAGE));
        Log.e(TAG, "Getting ID:" + hamtaIDString);
        studentList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list_view);

        // Skapar nya listan och exec
        new GetStudent().execute();
    }


    // SKAPA EGEN KLASS YAO ?

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetStudent extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Visar processdialog för main activity!
            pDialog = new ProgressDialog(SecondActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // skapa en bakgrunds-thread
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHanterare HH = new HttpHanterare();

            // Making a request to url and getting response
            String jsonStr = HH.requestURL(url + getHamtaIDString());

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr.length() > 1) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Requesting JSON object
                    JSONArray student = jsonObj.getJSONArray("courses");
                    for (int i = 0; i < student.length(); i++) {
                        JSONObject stud = student.getJSONObject(i);

                        String id = stud.getString("courseID");
                        String course = stud.getString("CourseCode");

                        HashMap<String, String> coursesHashMap = new HashMap<>();

                        // HashMap key: value
                        coursesHashMap.put("CourseID", id);
                        coursesHashMap.put("CourseCode", course);

                        studentList.add(coursesHashMap);
                    }
                } catch (final JSONException e) {
                    HashMap<String, String> coursesHashMap2 = new HashMap<>();
                    coursesHashMap2.put("No courses", "No courses found");
                    studentList.add(coursesHashMap2);

                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    Log.e(TAG, "Error in SecondActivity.java");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get Json/Courses from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get Json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    SecondActivity.this, studentList,
                    R.layout.list_item,
                    new String[]{"CourseID", "CourseCode"},
                    new int[]{R.id.CourseID, R.id.CourseCode});

            lv.setAdapter(adapter);
        }

    }
}


