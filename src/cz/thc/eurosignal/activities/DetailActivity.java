package cz.thc.eurosignal.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cz.thc.eurosignal.R;
import cz.thc.eurosignal.model.Model;
import cz.thc.eurosignal.model.SaneHttpResponse;

public class DetailActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.task_detail);
	((TextView) findViewById(R.id.taskName)).setText(Model.selectedTask
		.get("name"));
	((TextView) findViewById(R.id.textDescription))
		.setText(Model.selectedTask.get("description"));
	((TextView) findViewById(R.id.textState)).setText(Model.selectedTask
		.get("state", "name"));
	((TextView) findViewById(R.id.textOwner)).setText(Model.selectedTask
		.get("owner"));
	updateViewState();
    }

    public void onStart(View view) {
	Model.trackingTask = Model.selectedTask;
	updateViewState();
	new TrackingTaskPutTask().execute("");
    }

    public void onStop(View view) {
	Model.trackingTask = null;
	updateViewState();
    }

    public void onClose(View view) {
	startActivity(new Intent(this, MainActivity.class));
    }

    private void updateViewState() {
	if (isCurrentTaskTracking()) {
	    ((Button) findViewById(R.id.taskStart)).setEnabled(false);
	    ((Button) findViewById(R.id.taskStop)).setEnabled(true);
	    ((TextView) findViewById(R.id.textNowTracking))
		    .setVisibility(View.VISIBLE);
	} else {
	    ((Button) findViewById(R.id.taskStart)).setEnabled(true);
	    ((Button) findViewById(R.id.taskStop)).setEnabled(false);
	    ((TextView) findViewById(R.id.textNowTracking))
		    .setVisibility(View.GONE);
	}
    }

    private boolean isCurrentTaskTracking() {
	if (Model.selectedTask == null || Model.trackingTask == null)
	    return false;
	return Model.selectedTask.get("id").contentEquals(
		Model.trackingTask.get("id"));
    }

    private class TrackingTaskPutTask extends
	    AsyncTask<String, Void, SaneHttpResponse> {
	@Override
	protected SaneHttpResponse doInBackground(String... str) {
	    JSONObject jsonData = new JSONObject();
	    JSONObject userSession = new JSONObject();
	    try {
		userSession
			.put("current_task_id", Model.trackingTask.get("id"));
		jsonData.put("user", userSession);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    return Model.putJson("users", jsonData,
		    Model.trackingTask.get("user_id"));
	}

	@Override
	protected void onPostExecute(SaneHttpResponse response) {
	    if (response != null)
		Log.e("current task status", response.status);
	}
    }


    public void onDone(View view) {
	startActivity(new Intent(this, SignActivity.class));
    }



}
