package com.example.bassa.bloodpressuresynchronizer;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DBCursorAdapter databaseCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this); // create a database helper
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // get the data repository in write mode
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

        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
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
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(databaseCursorAdapter);
    }
}
