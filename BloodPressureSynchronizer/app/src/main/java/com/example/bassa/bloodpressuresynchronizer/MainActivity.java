package com.example.bassa.bloodpressuresynchronizer;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.SignatureType;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import static com.example.bassa.bloodpressuresynchronizer.DatabaseContract.BPEntry.TABLE_NAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences prefs;

    public static final int AUTHENTICATION_REQUEST = 1;

    public static OAuth10aService service;
    public static OAuth1RequestToken requestToken;

    private String user_id;
    private String oauth_verifier;
    private String accessTokenKey, accessTokenSecret;
    private OAuth1AccessToken accessToken;

    private static final String ENC = "UTF-8";

    private NavigationView navigationView;
    private TextView personalIDMessage;
    private ListView listView;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private DBCursorAdapter databaseCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(MainActivity.this); // create a database helper
        db = dbHelper.getWritableDatabase(); // get the data repository in write mode

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        accessTokenKey = prefs.getString("access_token_key", "");
        accessTokenSecret = prefs.getString("access_token_secret", "");
        user_id = prefs.getString("user_id", "");

        // http://stackoverflow.com/a/40258662/5572217
        boolean networkAvailable = isNetworkAvailable();
        if (accessTokenKey.isEmpty() || accessTokenSecret.isEmpty() || user_id.isEmpty()) {
            if (networkAvailable) {
                Intent intent = new Intent(this, WithingsAuthenticationActivity.class);
                startActivityForResult(intent, AUTHENTICATION_REQUEST);
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Internetiühendus puudub!")
                    .setMessage("Palun vajuta OK nuppu, taasta internetiühendus ja tee see rakendus uuesti lahti.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        } else {
            if (networkAvailable) {
                service = new ServiceBuilder()
                        .apiKey(WithingsAPI.API_KEY)
                        .apiSecret(WithingsAPI.API_SECRET)
                        .signatureType(SignatureType.QueryString)
                        .build(WithingsAPI.instance());
                accessToken = new OAuth1AccessToken(accessTokenKey, accessTokenSecret);

                initializeUI();
                getBPDataFromWithings();
            } else {
                initializeUI();
                populateListViewFromDB();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!accessTokenKey.isEmpty() & !accessTokenSecret.isEmpty() & !user_id.isEmpty()) {
            initializeUI();
        }
    }

    @Override
    // http://stackoverflow.com/a/40258662/5572217
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTHENTICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    user_id = extras.getString("USER_ID");
                    oauth_verifier = extras.getString("VERIFIER");
                    getAccessTokenThread.execute((Object) null);
                }
            } else {
                finish();
            }
        }
    }

    // http://stackoverflow.com/a/4239019/5572217
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // http://stackoverflow.com/a/40258662/5572217
    AsyncTask<Object, Object, Object> getAccessTokenThread = new AsyncTask<Object, Object, Object>() {
        @Override
        protected Object doInBackground(Object... params) {
            try {
                accessToken = service.getAccessToken(requestToken, oauth_verifier);
                accessTokenKey = accessToken.getToken();
                accessTokenSecret = accessToken.getTokenSecret();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // authentication complete
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("access_token_key", accessTokenKey);
            editor.putString("access_token_secret", accessTokenSecret);
            editor.putString("user_id", user_id);
            editor.apply();

            initializeUI();
            getBPDataFromWithings();
        };
    };

    private void initializeUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        SendBPDataNotificationService.updateActivity(this);
        PersonalInfoActivity.PersonalInfoFragment.updateActivity(this);
        SettingsActivity.SettingsFragment.updateActivity(this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        personalIDMessage = (TextView) findViewById(R.id.personalIDMessage);
        personalIDMessage.setVisibility(View.VISIBLE);
        if (isNetworkAvailable()) {
            personalIDMessage.setText(R.string.personal_id_needed_msg);
        } else {
            personalIDMessage.setText(R.string.no_network_msg);
        }

        // Set user's name and id to be shown in the navbar
        updateStuff(prefs, "user_first_name");
        updateStuff(prefs, "user_last_name");
        updateStuff(prefs, "user_personal_id");
        updateStuff(prefs, "synchronization_on");
        updateStuff(prefs, "notifications_on");

        if (!user_id.isEmpty() && isNetworkAvailable() && !prefs.getString("user_personal_id", "").isEmpty()) {
            Button syncBtn = (Button) findViewById(R.id.syncBtn);
            syncBtn.setVisibility(View.VISIBLE);
            syncBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getBPDataFromWithings();
                }
            });
        }

    }

    private void getBPDataFromWithings() {
        try {
            new OpenConnectionAndGetJSON().execute();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edit) {
            Intent personalInfoIntent = new Intent(this, PersonalInfoActivity.class);
            startActivity(personalInfoIntent);
        } else if (id == R.id.nav_manage) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void updateStuff(SharedPreferences shared, String key) {
        View header = navigationView.getHeaderView(0);

        if (key.equals("user_personal_id")) {
            String userID = shared.getString(key, "");
            TextView navBarSummary = (TextView) header.findViewById(R.id.navBarSummary);
            navBarSummary.setText(userID);

            if (!userID.isEmpty()) {
                // removes the annoying orange message
                personalIDMessage.setVisibility(View.GONE);
            } else {
                personalIDMessage.setVisibility(View.VISIBLE);
            }

            initializeAlarms("synchronization_frequency", true);
        }

        else if (key.equals("user_first_name") || key.equals("user_last_name")) {
            String firstName = shared.getString("user_first_name", "");
            String lastName = shared.getString("user_last_name", "");
            TextView navBarName = (TextView) header.findViewById(R.id.navBarName);
            navBarName.setText(firstName + " " + lastName);
        }

        else if (key.equals("synchronization_on")) {
            if (shared.getBoolean(key, true)) {
                initializeAlarms("synchronization_frequency", true);
                Log.i("SYNCHRONIZATION", "ON");
            } else {
                initializeAlarms("synchronization_frequency", false);
                Log.i("SYNCHRONIZATION", "OFF");
            }
        }

        else if (key.equals("synchronization_frequency")) {
            initializeAlarms(key, true);
        }

        else if (key.equals("notifications_on")) {
            if (shared.getBoolean(key, false)) {
                initializeAlarms("notification_frequency", true);
                Log.i("NOTIFICATIONS", "ON");
            } else {
                initializeAlarms("notification_frequency", false);
                Log.i("NOTIFICATIONS", "OFF");
            }
        }

        else if (key.equals("notification_frequency")) {
            initializeAlarms(key, true);
        }
    }

    private void initializeAlarms(String key, boolean initialize) {

        // initialize firing notifications
        // http://stackoverflow.com/a/16871244/5572217
        // http://karanbalkar.com/2013/07/tutorial-41-using-alarmmanager-and-broadcastreceiver-in-android/

        PendingIntent pendingIntent = null;
        if (key.equals("synchronization_frequency")) {
            Intent intent = new Intent(getApplication(), SendBPDataNotificationReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplication(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else if (key.equals("notification_frequency")) {
            Intent intent = new Intent(getApplication(), MeasureBPNotificationReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplication(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager am = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);

        if (initialize) {
            Calendar firingCal = Calendar.getInstance();
            Calendar currentCal = Calendar.getInstance();

            firingCal.set(Calendar.HOUR_OF_DAY, 10); // At the hour you wanna fire
            if (key.equals("synchronization_frequency")) {
                firingCal.set(Calendar.MINUTE, 15); // Particular minute
            } else if (key.equals("notification_frequency")) {
                firingCal.set(Calendar.MINUTE, 0); // Particular minute
            }
            firingCal.set(Calendar.SECOND, 0);

            long intendedTime = firingCal.getTimeInMillis();
            long currentTime = currentCal.getTimeInMillis();

            int frequency = Integer.valueOf(prefs.getString(key, "1"));
            Log.i("FREQUENCY", "" + frequency);

            if (intendedTime >= currentTime) { // 8.32, 9.57, etc.
                am.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, 60000*60*frequency, pendingIntent); // 60000 is one minute, 60000*60 is 60 minutes
            } else { // 18.30, 20.19
                int hours = currentCal.get(Calendar.HOUR_OF_DAY); // 18, 19

                firingCal.set(Calendar.HOUR_OF_DAY, hours + 1);

                intendedTime = firingCal.getTimeInMillis();
                am.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, 60000*60*frequency, pendingIntent);
            }
        }

    }

    static public class SendBPDataNotificationReceiver extends BroadcastReceiver {

        public SendBPDataNotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            Intent service = new Intent(context, MainActivity.SendBPDataNotificationService.class);
            context.startService(service);
        }

    }

    static public class SendBPDataNotificationService extends IntentService {

        private static WeakReference<MainActivity> mActivityRef;
        public static void updateActivity(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        public SendBPDataNotificationService() {
            super("SendBPDataNotificationService");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            MainActivity a = mActivityRef.get();

            if (!a.user_id.isEmpty()) {
                Log.i("SENDING BP DATA", "TO FAKE E-HEALTH");
                a.initializeUI();
                a.getBPDataFromWithings();
            }
        }

    }

    private class OpenConnectionAndGetJSON extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... params) throws RuntimeException {

            try {
                final OAuthRequest request = new OAuthRequest(Verb.GET, "http://wbsapi.withings.net/measure", service);
                request.addParameter("action", "getmeas");
                request.addParameter("userid", user_id);
                service.signRequest(accessToken, request); // the access token from step 4
                final Response response = request.send();
                return new JSONObject(response.getBody());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        protected void onPostExecute(JSONObject json) throws RuntimeException {
            String user_personal_id = prefs.getString("user_personal_id", "");
            if (!user_personal_id.isEmpty()) {
                try {
                    json.put("user_personal_id", user_personal_id);
                    new PostDataToServer().execute(json.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            dbHelper.deleteAll(db);
            addBPDataToDB(json);
            populateListViewFromDB();

            Toast.makeText(MainActivity.this, "Sünkroniseerisin andmed Withingsiga", Toast.LENGTH_LONG).show();
        }

    }

    private void addBPDataToDB(JSONObject json) {
        try {

            JSONArray measuregprs = json.getJSONObject("body").getJSONArray("measuregrps");
            int len_measuregrps = measuregprs.length();

            for (int i = 0; i < len_measuregrps; i++) {

                JSONObject grp = (JSONObject) measuregprs.get(i);
                JSONArray measures = grp.getJSONArray("measures");
                int len_measures = measures.length();
                if (len_measures != 3) continue; // if the measurement is not BP, but, for example, weight or height, then do not go any further

                int dia = 0;
                int sys = 0;
                int pulse = 0;

                for (int j = 0; j < len_measures; j++) { // otherwise go through a BP entry
                    JSONObject measure = (JSONObject) measures.get(j);
                    String type = measure.getString("type");
                    int value = measure.getInt("value");

                    if (type.equals("9")) { // Diastolic Blood Pressure (mmHg)
                        dia = value;
                    } else if (type.equals("10")) { // Systolic Blood Pressure (mmHg)
                        sys = value;
                    } else if (type.equals("11")) { // Heart Pulse (bpm)
                        pulse = value;
                    }
                }

                dbHelper.insert(db, sys, dia, pulse, grp.getString("date"));

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void populateListViewFromDB() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.BPEntry._ID,
                DatabaseContract.BPEntry.COLUMN_NAME_SYS,
                DatabaseContract.BPEntry.COLUMN_NAME_DIA,
                DatabaseContract.BPEntry.COLUMN_NAME_PULSE,
                DatabaseContract.BPEntry.COLUMN_NAME_DATE
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.BPEntry._ID + " ASC";

        Cursor c = db.query(
                TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        // Create an adapter to map values from the DB to the elements in the list view
        databaseCursorAdapter = new DBCursorAdapter(this, c, 0);
        databaseCursorAdapter.notifyDataSetChanged();

        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.emptyLabel));

        // Set the adapter for the list view
        listView.setAdapter(databaseCursorAdapter);
    }

    private class PostDataToServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) throws RuntimeException {

            String data = strings[0];
            try {
                URLConnection urlConnection = new URL("https://minu-tervis-veeb.herokuapp.com").openConnection();
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    total.append(line).append('\n');
                }
                wr.close();
                rd.close();

                return total.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        protected void onPostExecute(String str) throws RuntimeException {
            Log.i("RESPONSE_FROM_SERVER", str);

            Toast.makeText(MainActivity.this, "Sünkroniseerisin andmed Minu-tervisega", Toast.LENGTH_LONG).show();
        }

    };

}
