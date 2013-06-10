package com.muzima.view.sample.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.muzima.view.sample.R;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static String KEY_SERVER = "server";

    public static String KEY_USERNAME = "username";

    public static String KEY_PASSWORD = "password";

    public static String KEY_FIRST_RUN = "firstRun";

    public static String KEY_INFO = "info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);

        // Display app version number for user to know
        PreferenceScreen ps = (PreferenceScreen) findPreference(KEY_INFO);
        ps.setTitle(getString(R.string.app_name) + " v" + getString(R.string.app_version));

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.server_preferences));
        updateServer();
        updateUsername();
        updatePassword();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_SERVER)) {
            updateServer();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        }
    }

    private void updateServer() {
        EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
        String s = etp.getText();
        if (s.endsWith("/")) {
            s = s.substring(0, s.lastIndexOf("/"));
        }
        etp.setSummary(s);
    }

    private void updateUsername() {
        EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }

    private void updatePassword() {
        EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_PASSWORD);
        etp.setSummary(etp.getText().replaceAll(".", "*"));

    }
}
