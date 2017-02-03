package com.example.bassa.bloodpressuresynchronizer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class DBCursorAdapter extends SimpleCursorAdapter {

    private Context context;
    private Cursor c;

    private boolean checkBoxesShown;

    // When scrolling a list view, the state of the checkboxes (which ones were checked and which ones weren't) is not saved
    // This array will store the state of the each checkbox
    private ArrayList<Boolean> checkedBoxes = new ArrayList<>();

    public DBCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);

        this.context = context;
        this.c = c;

        checkBoxesShown = false;

        // Initialize the ArrayList items to default value false, meaning no checkbox is checked yet
        int n = this.getCount();
        for (int i = 0; i < n; i++) {
            checkedBoxes.add(i, false);
        }
    }

    public View getView(final int pos, View convertView, ViewGroup parent) { // gets called for every single row in a list view before that row is shown to the user
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.entry_layout, null);
        } else {
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

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

        return convertView;
    }

    public void setCheckBoxesShown(boolean b) {
        checkBoxesShown = b;
    }

    public ArrayList<Boolean> getCheckedBoxes() {
        return checkedBoxes;
    }

}
