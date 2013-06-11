package com.muzima.view.sample.activities;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.muzima.api.context.Context;
import com.muzima.api.context.ContextFactory;
import com.muzima.api.model.Observation;
import com.muzima.api.model.Patient;
import com.muzima.api.service.ObservationService;
import com.muzima.api.service.PatientService;
import com.muzima.view.sample.R;
import com.muzima.view.sample.adapters.EncounterAdapter;
import com.muzima.view.sample.utilities.StringConstants;
import com.muzima.view.sample.utilities.FileUtils;
import org.apache.lucene.queryParser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class ObservationTimelineActivity extends ListActivity {

    private static final String TAG = ObservationChartActivity.class.getSimpleName();

    private Patient patient;

    private String fieldUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.observation_timeline);

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, R.string.storage_error));
            finish();
        }

        String patientUuid = getIntent().getStringExtra(StringConstants.KEY_PATIENT_ID);
        patient = getPatient(patientUuid);

        fieldUuid = getIntent().getStringExtra(StringConstants.KEY_OBSERVATION_FIELD_ID);
        String fieldName = getIntent().getStringExtra(StringConstants.KEY_OBSERVATION_FIELD_NAME);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.view_patient_detail));

        TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setText(fieldName);
    }

    private Context getContext() throws Exception {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String server = settings.getString(
                PreferencesActivity.KEY_SERVER, getString(R.string.default_server));
        String username = settings.getString(
                PreferencesActivity.KEY_USERNAME, getString(R.string.default_username));
        String password = settings.getString(
                PreferencesActivity.KEY_PASSWORD, getString(R.string.default_password));
        Context context = ContextFactory.createContext();
        try {
            if (!context.isAuthenticated())
                context.authenticate(username, password, server);
        } catch (ParseException e) {
            Log.e(TAG, "Unable to authenticate the current context.", e);
        }

        return context;
    }

    private Patient getPatient(final String uuid) {
        Patient patient = null;
        try {
            PatientService patientService = getContext().getPatientService();
            patient = patientService.getPatientByUuid(uuid);
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        }
        return patient;
    }

    private void getObservations(final String patientUuid, final String conceptUuid) {
        List<Observation> observations = new ArrayList<Observation>();
        try {
            ObservationService observationService = getContext().getObservationService();
            observations = observationService.getObservationsByPatientAndConcept(patientUuid, conceptUuid);
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        }
        ArrayAdapter<Observation> observationAdapter = new EncounterAdapter(this, R.layout.encounter_list_item, observations);
        setListAdapter(observationAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (patient != null && fieldUuid != null) {
            getObservations(patient.getUuid(), fieldUuid);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast_view, null);

        // set the text in the view
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(message);

        Toast t = new Toast(this);
        t.setView(view);
        t.setDuration(Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}
