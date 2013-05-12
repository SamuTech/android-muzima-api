package com.mclinic.view.sample.tasks;

import android.util.Log;
import android.widget.ProgressBar;
import com.mclinic.api.context.Context;
import com.mclinic.api.context.ContextFactory;
import com.mclinic.api.model.Cohort;
import com.mclinic.api.model.Member;
import com.mclinic.api.service.CohortService;
import com.mclinic.api.service.ObservationService;
import com.mclinic.api.service.PatientService;
import com.mclinic.search.api.util.StringUtil;
import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;
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
            for (Cohort cohort : cohorts) {
                List<Member> members = cohortService.downloadMembers(cohort.getUuid());
                for (Member member : members) {
                    patientService.downloadPatientByUuid(member.getPatientUuid());
                    observationService.downloadObservationsByPatient(member.getPatientUuid());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        } catch (ParseException e) {
            Log.e(TAG, "Exception when trying to load patient", e);
        } finally {
            if (context != null)
                context.closeSession();
        }

        return returnValue;
    }
}