package com.mclinic.view.sample.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.muzima.api.context.Context;
import com.muzima.api.context.ContextFactory;
import com.muzima.api.model.Observation;
import com.muzima.api.model.Patient;
import com.muzima.api.service.ObservationService;
import com.muzima.api.service.PatientService;
import com.muzima.util.Constants;
import com.mclinic.view.sample.R;
import com.mclinic.view.sample.adapters.ObservationAdapter;
import com.mclinic.view.sample.utilities.StringConstants;
import com.mclinic.view.sample.utilities.FileUtils;
import org.apache.lucene.queryParser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewPatientActivity extends ListActivity {

    private static final String TAG = ObservationChartActivity.class.getSimpleName();

    private static Patient patient;

    private ArrayAdapter<Observation> observationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_patient);

        setListAdapter(observationAdapter);

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, R.string.storage_error));
            finish();
        }

        String patientUuid = getIntent().getStringExtra(StringConstants.KEY_PATIENT_ID);
        patient = getPatient(patientUuid);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.view_patient));

        TextView textView = (TextView) findViewById(R.id.identifier_text);
        textView.setText(patient.getIdentifier());

        textView = (TextView) findViewById(R.id.name_text);
        textView.setText(patient.getGivenName() + " " + patient.getMiddleName() + " " + patient.getFamilyName());

        DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
        textView = (TextView) findViewById(R.id.birthdate_text);
        textView.setText(df.format(patient.getBirthdate()));

        ImageView imageView = (ImageView) findViewById(R.id.gender_image);
        if (imageView != null) {
            if (patient.getGender().equals("M")) {
                imageView.setImageResource(R.drawable.male);
            } else if (patient.getGender().equals("F")) {
                imageView.setImageResource(R.drawable.female);
            }
        }
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

        context.openSession();
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

    private void getAllObservations(final String patientUuid) {
        List<Observation> observations = new ArrayList<Observation>();
        try {
            ObservationService observationService = getContext().getObservationService();
            observations = observationService.getObservationsByPatient(patientUuid);
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        }
        observationAdapter = new ObservationAdapter(this, R.layout.observation_list_item, observations);
        setListAdapter(observationAdapter);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {

        if (patient != null) {
            Observation obs = (Observation) getListAdapter().getItem(position);

            Intent ip;
            int dataType = obs.getDataType();
            if (dataType == Constants.TYPE_NUMERIC) {
                ip = new Intent(getApplicationContext(), ObservationChartActivity.class);
                ip.putExtra(StringConstants.KEY_PATIENT_ID, patient.getUuid());
                ip.putExtra(StringConstants.KEY_OBSERVATION_FIELD_ID, obs.getQuestionUuid());
                ip.putExtra(StringConstants.KEY_OBSERVATION_FIELD_NAME, obs.getQuestionName());
                startActivity(ip);
            } else {
                ip = new Intent(getApplicationContext(), ObservationTimelineActivity.class);
                ip.putExtra(StringConstants.KEY_PATIENT_ID, patient.getUuid());
                ip.putExtra(StringConstants.KEY_OBSERVATION_FIELD_ID, obs.getQuestionUuid());
                ip.putExtra(StringConstants.KEY_OBSERVATION_FIELD_NAME, obs.getQuestionName());
                startActivity(ip);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patient != null) {
            getAllObservations(patient.getUuid());
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
