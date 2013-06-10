package com.mclinic.view.sample.tasks;

import android.util.Log;
import android.widget.ProgressBar;
import com.muzima.api.context.Context;
import com.muzima.api.context.ContextFactory;
import com.muzima.api.model.Cohort;
import com.muzima.api.model.CohortData;
import com.muzima.api.model.CohortMember;
import com.muzima.api.model.Observation;
import com.muzima.api.model.Patient;
import com.muzima.api.service.CohortService;
import com.muzima.api.service.ObservationService;
import com.muzima.api.service.PatientService;
import com.muzima.search.api.util.StringUtil;

import java.util.List;

public class DownloadPatientTask extends DownloadTask {

    private static final String TAG = DownloadPatientTask.class.getSimpleName();

    private ProgressBar progressBar;

    public DownloadPatientTask(final ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected void onPostExecute(final String s) {
        super.onPostExecute(s);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    protected String doInBackground(String... values) {
        String returnValue = StringUtil.EMPTY;
        String username = values[0];
        String password = values[1];
        String server = values[2];

        Context context = null;
        try {
            context = ContextFactory.createContext();
            context.openSession();
            context.authenticate(username, password, server);

            CohortService cohortService = context.getCohortService();
            PatientService patientService = context.getPatientService();
            ObservationService observationService = context.getObservationService();

            List<Cohort> cohorts = cohortService.downloadCohortsByName(StringUtil.EMPTY);
            if (!cohorts.isEmpty()) {
                Cohort selectedCohort = cohorts.get(0);
                cohortService.saveCohort(selectedCohort);
                CohortData cohortData = cohortService.downloadCohortData(selectedCohort.getUuid(), false);
                for (Patient patient : cohortData.getPatients()) {
                    List<Observation> observations = observationService.downloadObservationsByPatient(patient.getUuid());
                    for (Observation observation : observations) {
                        observationService.saveObservation(observation);
                    }
                    patientService.savePatient(patient);
                }

                for (CohortMember cohortMember : cohortData.getCohortMembers()) {
                    cohortService.saveCohortMember(cohortMember);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        } finally {
            if (context != null)
                context.closeSession();
        }

        return returnValue;
    }
}