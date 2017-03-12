package com.example.bassa.bloodpressuresynchronizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DBCursorAdapter databaseCursorAdapter;

    private static NavigationView navigationView;
    private ListView listView;

    private SharedPreferences prefs;

    public static final int AUTHENTICATION_REQUEST = 1;

    public static OAuth10aService service;
    public static OAuth1RequestToken requestToken;
    private String accessTokenKey, accessTokenSecret;
    private OAuth1AccessToken accessToken;
    private String oauth_verifier = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        accessTokenKey = prefs.getString("access_token_key", "");
        accessTokenSecret = prefs.getString("access_token_secret", "");

        // http://stackoverflow.com/a/40258662/5572217
        if (accessTokenKey.isEmpty() && accessTokenSecret.isEmpty()) {
            startAuthenticationActivity();
        } else {
            service = new ServiceBuilder()
                    .apiKey(WithingsAPI.API_KEY)
                    .apiSecret(WithingsAPI.API_SECRET)
                    .build(WithingsAPI.instance());
            accessToken = new OAuth1AccessToken(accessTokenKey, accessTokenSecret);

            initializeUI();
        }
    }

    // http://stackoverflow.com/a/40258662/5572217
    private void startAuthenticationActivity() {
        Intent intent = new Intent(this, WithingsAuthenticationActivity.class);
        startActivityForResult(intent, AUTHENTICATION_REQUEST);
    }

    @Override
    // http://stackoverflow.com/a/40258662/5572217
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.w("WITHINS_SUCCESS_LOG", "onActivityResult(int requestCode, int resultCode, Intent intent)");

        if (requestCode == AUTHENTICATION_REQUEST) {

            if (resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    oauth_verifier = extras.getString("VERIFIER");
                    getAccessTokenThread.execute((Object) null);
                }
            }

        }
    }

    // http://stackoverflow.com/a/40258662/5572217
    AsyncTask<Object, Object, Object> getAccessTokenThread = new AsyncTask<Object, Object, Object>() {
        @Override
        protected Object doInBackground(Object... params) {
            try {
                accessToken = service.getAccessToken(requestToken, oauth_verifier);
            } catch (IOException e) {
                e.printStackTrace();
            }

            accessTokenKey = accessToken.getToken();
            accessTokenSecret = accessToken.getTokenSecret();

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // authentication complete
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("access_token_key", accessTokenKey);
            editor.putString("access_token_secret", accessTokenSecret);
            editor.apply();

            initializeUI();
        };
    };

    private void initializeUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set user's name and sex to be shown in the navbar
        updateNavBarInfo(prefs, "user_name");
        updateNavBarInfo(prefs, "user_id");

        final DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this); // create a database helper
        final SQLiteDatabase db = dbHelper.getWritableDatabase(); // get the data repository in write mode
        populateListViewFromDB(db); // get the data from database and show it in the UI

        // Start initializing views

        final LinearLayout bottomBtns = (LinearLayout) findViewById(R.id.bottomBtns);

        final Button selectEntriesBtn = (Button) findViewById(R.id.selectEntriesBtn);
        selectEntriesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Tell the adapter to show the checkboxes
                databaseCursorAdapter.setCheckBoxesShown(true);
                databaseCursorAdapter.notifyDataSetChanged();

                // Remove yourself
                v.setVisibility(View.GONE);

                // Add 'Cancel', 'Send to doctor' and 'Delete' buttons instead
                bottomBtns.setVisibility(View.VISIBLE);
            }
        });

        final Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Tell the adapter to hide the checkboxes
                databaseCursorAdapter.setCheckBoxesShown(false);
                databaseCursorAdapter.notifyDataSetChanged();

                // Remove yourself
                bottomBtns.setVisibility(View.GONE);

                // Add 'Select entries' button instead
                selectEntriesBtn.setVisibility(View.VISIBLE);
            }
        });

        Button deleteBtn = (Button) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Remove the rows from our database
                ArrayList<Boolean> checkedBoxes = databaseCursorAdapter.getCheckedBoxes();
                int n = checkedBoxes.size();
                for (int i = 0; i < n; i++) {
                    if (checkedBoxes.get(i)) {
                        long id = databaseCursorAdapter.getItemId(i);
                        dbHelper.delete(db, id);
                    }
                }

                // Requery the cursor
                databaseCursorAdapter.getCursor().requery();
                cancelBtn.performClick();
            }
        });
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        // Shows a message when there are no BP data entries in the database
        View empty = findViewById(R.id.emptyLabel);
        ListView list = (ListView) findViewById(R.id.listView);
        list.setEmptyView(empty);
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected static void updateNavBarInfo(SharedPreferences shared, String key) {
        View header = navigationView.getHeaderView(0);

        if (key.equals("user_name")) {
            String userName = shared.getString("user_name", "");
            TextView navBarName = (TextView) header.findViewById(R.id.navBarName);
            navBarName.setText(userName);
        } else if (key.equals("user_id")) {
            String userID = shared.getString("user_id", "");
            TextView navBarSummary = (TextView) header.findViewById(R.id.navBarSummary);
            navBarSummary.setText(userID);
        }
    }

    private void populateListViewFromDB(SQLiteDatabase db) {
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
        String sortOrder = DatabaseContract.BPEntry._ID + " DESC";

        Cursor c = db.query(
                DatabaseContract.BPEntry.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        String[] columns = {
                DatabaseContract.BPEntry.COLUMN_NAME_SYS,
                DatabaseContract.BPEntry.COLUMN_NAME_DIA,
                DatabaseContract.BPEntry.COLUMN_NAME_PULSE,
                DatabaseContract.BPEntry.COLUMN_NAME_DATE
        };
        int[] IDs = new int[] {R.id.sysRate, R.id.diaRate, R.id.pulseRate, R.id.dateLabel};

        // Create an adapter to map values from the DB to the elements in the list view
        databaseCursorAdapter = new DBCursorAdapter(
                this,
                R.layout.entry_layout,
                c,
                columns,
                IDs
        );

        // Set the adapter for the list view
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(databaseCursorAdapter);
    }

}
