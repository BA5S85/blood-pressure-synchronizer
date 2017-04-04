package com.example.bassa.bloodpressuresynchronizer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // http://stackoverflow.com/a/34222656/5572217
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PersonalInfoFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // http://stackoverflow.com/a/34222656/5572217
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PersonalInfoFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static WeakReference<MainActivity> mActivityRef;
        public static void updateActivity(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        private SharedPreferences sharedPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.personal_info);

            sharedPreferences = getPreferenceManager().getSharedPreferences();

            // Put preferences values as summaries
            fillSummaries(getPreferenceScreen());

            EditTextPreference editPref = (EditTextPreference) getPreferenceScreen().findPreference("user_personal_id");
            editPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String str = newValue.toString();
                    try {
                        Integer.parseInt(str);
                        int firstNumber = Integer.parseInt("" + str.charAt(0)); // 1. = sugu ja sünni nn. "sajand" (praegune vahemik 1...6)
                        int fourthAndFifthNumber = Integer.parseInt(str.substring(3, 5)); // 4. ja 5. = sünnikuu (01...12)
                        int sixthAndSeventhNumber = Integer.parseInt(str.substring(5, 7));// 6. ja 7. = sünnikuupäev (01...31)

                        if (str.length() == 11 &&
                                firstNumber >= 1 && firstNumber <= 6 &&
                                fourthAndFifthNumber >= 1 && fourthAndFifthNumber <= 12 &&
                                sixthAndSeventhNumber >= 1 && sixthAndSeventhNumber <= 31) {
                            return true;
                        } else {
                            Toast.makeText(getActivity(), "Vigane isikukood!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Vigane isikukood!", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        // http://stackoverflow.com/a/4325239/5572217

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefSummary(findPreference(key));
            MainActivity a = mActivityRef.get();
            a.updateStuff(sharedPreferences, key);
        }

        private void fillSummaries(Preference p) {
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) p;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    fillSummaries(pGrp.getPreference(i));
                }
            } else {
                updatePrefSummary(p);
            }
        }

        private void updatePrefSummary(Preference p) {
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(editTextPref.getText());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(editTextPref.getKey(), editTextPref.getText());
                editor.apply();
            }
        }
    }

}
