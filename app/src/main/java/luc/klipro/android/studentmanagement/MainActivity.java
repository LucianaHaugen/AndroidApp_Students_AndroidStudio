package luc.klipro.android.studentmanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private ListAdapter adapter;

    // Server address: URL to get contacts JSON
    public static String url = "http://localhost:8080/students?format=json&id=all";

    public ArrayList<HashMap<String, String>> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create and populate ListView
        studentList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list_view);
        new GetStudent().execute();


        // test med click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                Log.e(LOG_TAG, "Chosen Student ID:" + (position+1));
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                int positionInt = (position+1);
                String positionMessage = String.valueOf(positionInt);
                intent.putExtra(EXTRA_MESSAGE, positionMessage);
                startActivity(intent);
            }
        });

    }


    // AsyncTask
    private class GetStudent extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Dialog to update user
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        // Create background tread
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHanterare HH = new HttpHanterare();

            String jsonStr = HH.requestURL(url);
            Log.e(LOG_TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    // Request JSON object
                    JSONArray student = jsonObject.getJSONArray("students");

                    for (int i = 0; i < student.length(); i++) {
                        JSONObject studentJSONObject = student.getJSONObject(i);

                        String id = studentJSONObject.getString("studentID");
                        String name = studentJSONObject.getString("studentName");

                        HashMap<String, String> studentHashMap = new HashMap<>();
                        // HashMap key: value
                        studentHashMap.put("studentID", id);
                        studentHashMap.put("studentName", name);

                        studentList.add(studentHashMap);
                    }
                } catch (final JSONException je) {
                    Log.e(LOG_TAG, "Json parsing error: " + je.getMessage());
                    Log.e(LOG_TAG, "Error in Main Activity");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + je.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(LOG_TAG, "Couldn't get 'Student' from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get 'Student' from server. Check LogCat for possible errors!",
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
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, studentList,
                    R.layout.list_item, new String[] {"studentName", "studentID"},
                    new int[]{R.id.studentName, R.id.studentID});

            lv.setAdapter(adapter);
        }

    }
}

