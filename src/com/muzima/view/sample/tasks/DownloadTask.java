package com.muzima.view.sample.tasks;

import android.os.AsyncTask;
import com.muzima.view.sample.listeners.DownloadListener;

public abstract class DownloadTask extends AsyncTask<String, String, String> {

	protected DownloadListener mStateListener;

	@Override
	protected void onProgressUpdate(String... values) {
		synchronized (this) {
			if (mStateListener != null) {
				// update progress and total
				if (values.length > 1)
					mStateListener.progressUpdate(values[0], Integer.valueOf(values[1]), Integer.valueOf(values[2]));
				else
					mStateListener.progressUpdate(values[0]);
			}
		}
	}

	@Override
	protected void onPostExecute(String result) {
		synchronized (this) {
			if (mStateListener != null) {
				mStateListener.taskComplete(result);
			}
		}
	}

	public void setDownloadListener(DownloadListener dl) {
		synchronized (this) {
			mStateListener = dl;
		}
	}
}