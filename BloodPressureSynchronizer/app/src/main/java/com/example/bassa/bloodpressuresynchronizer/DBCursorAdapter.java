package com.example.bassa.bloodpressuresynchronizer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DBCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    private boolean checkBoxesShown;

    // When scrolling a list view, the state of the checkboxes (which ones were checked and which ones weren't) is not saved
    // This array will store the state of the each checkbox
    private ArrayList<Boolean> checkedBoxes = new ArrayList<>();

    protected DBCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        checkBoxesShown = false;

        // Initialize the ArrayList items to default value false, meaning no checkbox is checked yet
        int n = this.getCount();
        for (int i = 0; i < n; i++) {
            checkedBoxes.add(i, false);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView sysRate = (TextView) view.findViewById(R.id.sysRate);
        TextView diaRate = (TextView) view.findViewById(R.id.diaRate);
        TextView pulseRate = (TextView) view.findViewById(R.id.pulseRate);
        TextView dateLabel = (TextView) view.findViewById(R.id.dateLabel);

        String sys = cursor.getString(cursor.getColumnIndex(DatabaseContract.BPEntry.COLUMN_NAME_SYS));
        String dia = cursor.getString(cursor.getColumnIndex(DatabaseContract.BPEntry.COLUMN_NAME_DIA));
        String pulse = cursor.getString(cursor.getColumnIndex(DatabaseContract.BPEntry.COLUMN_NAME_PULSE));
        String unixTime = cursor.getString(cursor.getColumnIndex(DatabaseContract.BPEntry.COLUMN_NAME_DATE));

        // http://stackoverflow.com/a/17433005/5572217
        int unixSeconds = Integer.parseInt(unixTime);
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);

        sysRate.setText(sys);
        diaRate.setText(dia);
        pulseRate.setText(pulse);
        dateLabel.setText(formattedDate);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        final int pos = cursor.getPosition();

        if (checkBoxesShown) {
            checkBox.setVisibility(View.VISIBLE);

            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        checkedBoxes.set(pos, true);
                    } else if (!checkBox.isChecked()) {
                        checkedBoxes.set(pos, false);
                    }
                }
            });

            // Set the state of the checkbox according to what it was before the list view lost its state
            checkBox.setChecked(checkedBoxes.get(pos));
        } else {
            checkedBoxes.set(pos, false);
            checkBox.setChecked(false);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.entry_layout, parent, false);
    }

    public void setCheckBoxesShown(boolean b) {
        checkBoxesShown = b;
    }

}
