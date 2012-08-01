package cz.thc.eurosignal.activities;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import cz.thc.eurosignal.R;
import cz.thc.eurosignal.model.Model;
import cz.thc.eurosignal.model.SaneHttpResponse;
import cz.thc.eurosignal.model.TaskJson;
import cz.thc.eurosignal.services.LocationUpdateService;

public class LoginActivity extends TabActivity {

	//	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	//		@Override
	//		public void onReceive(Context context, Intent intent) {
	//			Log.e("Broadcast","RECEIVE!");
	//		}
	//	};


	private Context context;

	private class LoginPostTask extends AsyncTask<String, Void, SaneHttpResponse> {
		@Override
		protected SaneHttpResponse doInBackground(String... str) {
			JSONObject jsonData = new JSONObject();
			JSONObject userSession = new JSONObject();
			try {
				userSession.put("login", str[0]);
				userSession.put("password", str[1]);
				jsonData.put("user_session",userSession);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return Model.postJson("session/auth.json",jsonData);
		}

		@Override
		protected void onPostExecute(SaneHttpResponse response) {
			Model.token = null;
			Log.e("login status",response.status);
			if (response.code == 200) {
				getChildTab(1).setEnabled(true);
				JSONObject user = response.getJsonObject();
				Model.currentUser = user;
				try {
					JSONObject task = user.getJSONObject("current_task");
					Model.trackingTask = new TaskJson(task);
				} catch (JSONException e) {
					Log.e("login status","JSON user task missing");	
				}

				try{
					Model.token = user.getString("single_access_token");
				} catch (JSONException e) {
					Log.e("login status","JSON token missing");				}
			}
			if (Model.token != null){
				Model.isLoggedIn = true;
				findViewById(R.id.loginBox).setVisibility(View.GONE);
				findViewById(R.id.trackingBox).setVisibility(View.VISIBLE);
				getTextView(R.id.loginStatusView).setVisibility(View.GONE);
				new TasksGetTask().execute("");
			} else {
				getChildTab(1).setEnabled(false);
				findViewById(R.id.loginBox).setVisibility(View.VISIBLE);
				findViewById(R.id.trackingBox).setVisibility(View.GONE);
				getTextView(R.id.loginStatusView).setVisibility(View.VISIBLE);
				Model.token = null;
				Model.isLoggedIn = false;				
			}
		}
	}

	private class LogoutPostTask extends AsyncTask<String, Void, SaneHttpResponse> {
		@Override
		protected SaneHttpResponse doInBackground(String... str) {
			return Model.getJson("session/logout.json");
		}

		@Override
		protected void onPostExecute(SaneHttpResponse response) {			
			findViewById(R.id.loginBox).setVisibility(View.VISIBLE);
			findViewById(R.id.trackingBox).setVisibility(View.GONE);
			Model.token = null;
			Model.isLoggedIn = false;
			getChildTab(1).setEnabled(false);

		}
	}

	private class TasksGetTask extends AsyncTask<String, Void, SaneHttpResponse> {
		@Override
		protected SaneHttpResponse doInBackground(String... str) {
			return Model.getJson("tasks.json");
		}

		@Override
		protected void onPostExecute(SaneHttpResponse response) {			
			//				findViewById(R.id.loginBox).setVisibility(View.VISIBLE);
			//				findViewById(R.id.trackingBox).setVisibility(View.GONE);
			//				Model.token = null;
			//				Model.isLoggedIn = false;
			//				getChildTab(1).setEnabled(false);
			Log.d("TASKS RESPONSE", response.body);
			if (response.getList() != null) {
				Model.tasks = new ArrayList<TaskJson>();
				for (JSONObject task : response.getList()){
					Model.tasks.add(new TaskJson(task));
				}
			}
			if (Model.tasks==null || Model.tasks.isEmpty()) return;
			ListView listView = (ListView)findViewById(R.id.taskList);
			ArrayAdapter<TaskJson> adapter = new ArrayAdapter<TaskJson>(context, android.R.layout.simple_list_item_1 ,android.R.id.text1, Model.tasks);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Model.selectedTask = Model.tasks.get(position);
					Intent i = new Intent(getApplicationContext(), DetailActivity.class);
					startActivity(i);
				}

			});		}
	}



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		Model.locationUpdateServiceIntent = new Intent(this, LocationUpdateService.class);
		setContentView(R.layout.main);
		TabHost mTabHost = getTabHost();
		mTabHost.addTab(
				mTabHost.newTabSpec("account").setIndicator("Account",
						getResources().getDrawable(R.drawable.ic_info)).setContent(R.id.account));


		mTabHost.addTab(
				mTabHost.newTabSpec("tasks").setIndicator("Tasks",
						getResources().getDrawable(R.drawable.ic_star)).setContent(R.id.taskList));

		mTabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);

		getChildTab(1).setEnabled(false);
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {			
			public void onTabChanged(String tabId) {
				new TasksGetTask().execute("");
			}
		});
	}

	private View getChildTab(int i){
		return getTabHost().getTabWidget().getChildTabViewAt(i);
	}


	public void onStartTracking(View view){
		startService(Model.locationUpdateServiceIntent);
		//			registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdateService.BROADCAST_ACTION));
		getButton(R.id.startTrackingButton).setVisibility(View.GONE);
		getButton(R.id.stopTrackingButton).setVisibility(View.VISIBLE);
		Model.isTracking = true;
	}

	public void onStopTracking(View view){		
		stopService(Model.locationUpdateServiceIntent);
		//		if (broadcastReceiver.) unregisterReceiver(broadcastReceiver);
		getButton(R.id.startTrackingButton).setVisibility(View.VISIBLE);
		getButton(R.id.stopTrackingButton).setVisibility(View.GONE);
		Model.isTracking = false;		
	}

	public void onLoginClick(View view){
		new LoginPostTask().execute(
				getEditText(R.id.usernameField).getText().toString(),
				getEditText(R.id.passwordField).getText().toString());
	}

	public void onLogoutClick(View view){
		onStopTracking(view);		
		new LogoutPostTask().execute("");
	}

	public TextView getTextView(int id) {
		return (TextView)findViewById(id);
	}

	public ImageView getImageView(int id) {
		return (ImageView)findViewById(id);
	}

	public EditText getEditText(int id) {
		return (EditText)findViewById(id);
	}

	public Button getButton(int id) {
		return (Button)findViewById(id);
	}

}