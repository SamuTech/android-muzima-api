package com.muzima.view.sample.tasks;

import android.util.Log;
import android.widget.ProgressBar;
import com.muzima.api.context.Context;
import com.muzima.api.context.ContextFactory;
import com.muzima.api.model.Cohort;
import com.muzima.api.model.CohortData;
import com.muzima.api.model.Observation;
import com.muzima.api.model.Patient;
import com.muzima.api.service.CohortService;
import com.muzima.api.service.ObservationService;
import com.muzima.api.service.PatientService;
import com.muzima.search.api.util.StringUtil;
import com.muzima.util.Constants;
import junit.framework.Assert;

import java.io.File;
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
            if (!context.isAuthenticated()) {
                context.authenticate(username, password, server);
            }

            ContextFactory.getProperty(Constants.LUCENE_DIRECTORY_NAME);
            File luceneDirectory = new File(System.getProperty("java.io.tmpdir") + "/lucene");
            for (String filename : luceneDirectory.list()) {
                File file = new File(luceneDirectory, filename);
                Assert.assertTrue(file.delete());
            }

            int patientCounter = 0;
            int cohortMemberCounter = 0;
            int observationCounter = 0;

            CohortService cohortService = context.getCohortService();
            PatientService patientService = context.getPatientService();
            ObservationService observationService = context.getObservationService();

            List<Cohort> cohorts = cohortService.downloadCohortsByName(StringUtil.EMPTY);

            long start = System.currentTimeMillis();
            if (!cohorts.isEmpty()) {
                Cohort selectedCohort = cohorts.get(0);
                cohortService.saveCohort(selectedCohort);
                CohortData cohortData = cohortService.downloadCohortData(selectedCohort.getUuid(), false);
                for (Patient patient : cohortData.getPatients()) {
                    List<Observation> observations = observationService.downloadObservationsByPatient(patient.getUuid());
                    observationService.saveObservations(observations);
                    observationCounter = observationCounter + observations.size();
                }
                patientCounter = patientCounter + cohortData.getPatients().size();
                patientService.savePatients(cohortData.getPatients());
                cohortMemberCounter = cohortMemberCounter + cohortData.getCohortMembers().size();
                cohortService.saveCohortMembers(cohortData.getCohortMembers());
            }
            long end = System.currentTimeMillis();
            double elapsed = (end - start) / 1000;
            Log.i(TAG, "Download Statistic:");
            Log.i(TAG, "Total time: " + elapsed + "s");
            Log.i(TAG, "Total patients: " + patientCounter);
            Log.i(TAG, "Total cohort members: " + cohortMemberCounter);
            Log.i(TAG, "Total observations: " + observationCounter);
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        } finally {
            if (context != null)
                context.closeSession();
        }

        return returnValue;
    }
}