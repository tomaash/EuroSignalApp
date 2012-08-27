package cz.thc.eurosignal.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import cz.thc.eurosignal.R;
import cz.thc.eurosignal.model.Model;
import cz.thc.eurosignal.model.SaneHttpResponse;
import cz.thc.eurosignal.model.TaskJson;

public class LoginActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	if (Model.isLoggedIn)
	    loggedIn();
	else
	    setContentView(R.layout.login);

    }


    public void onLoginClick(View view) {
	new LoginPostTask().execute(getEditText(R.id.usernameField).getText()
		.toString(), getEditText(R.id.passwordField).getText()
		.toString());
    }

    private void loggedIn() {
	findViewById(R.id.loginStatusView).setVisibility(View.GONE);
	startActivity(new Intent(this, MainActivity.class));
	finish();
    }


    public EditText getEditText(int id) {
	return (EditText) findViewById(id);
    }

    private class LoginPostTask extends
	    AsyncTask<String, Void, SaneHttpResponse> {
	@Override
	protected SaneHttpResponse doInBackground(String... str) {
	    JSONObject jsonData = new JSONObject();
	    JSONObject userSession = new JSONObject();
	    try {
		userSession.put("login", str[0]);
		userSession.put("password", str[1]);
		jsonData.put("user_session", userSession);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    return Model.postJson("session/auth.json", jsonData);
	}

	@Override
	protected void onPostExecute(SaneHttpResponse response) {
	    Model.token = null;
	    Log.i("login status", response.status);
	    if (response.code == 200) {
		JSONObject user = response.getJsonObject();
		Model.currentUser = user;
		try {
		    JSONObject task = user.getJSONObject("current_task");
		    Model.trackingTask = new TaskJson(task);
		} catch (JSONException e) {
		    Log.e("login status", "JSON user task missing");
		}

		try {
		    Model.token = user.getString("single_access_token");
		} catch (JSONException e) {
		    Log.e("login status", "JSON token missing");
		}
	    }
	    if (Model.token != null) {
		Model.isLoggedIn = true;
		loggedIn();

	    } else {
		findViewById(R.id.loginStatusView).setVisibility(View.VISIBLE);
		Model.token = null;
		Model.isLoggedIn = false;
	    }
	}
    }
}
