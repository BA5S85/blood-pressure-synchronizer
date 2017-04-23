package com.example.bassa.bloodpressuresynchronizer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DBCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;

    protected DBCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.entry_layout, parent, false);
    }

}
