package cz.thc.eurosignal.activities;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import cz.thc.eurosignal.R;
import cz.thc.eurosignal.model.Model;
import cz.thc.eurosignal.model.SaneHttpResponse;
import cz.thc.eurosignal.model.TaskJson;
import cz.thc.eurosignal.services.LocationUpdateService;

public class MainActivity extends TabActivity {

    //	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    //		@Override
    //		public void onReceive(Context context, Intent intent) {
    //			Log.e("Broadcast","RECEIVE!");
    //		}
    //	};



    private void loggedOut() {
	startActivity(new Intent(this, LoginActivity.class));
    }




    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Model.locationUpdateServiceIntent = new Intent(this,
		LocationUpdateService.class);
	setContentView(R.layout.main);
	TabHost mTabHost = getTabHost();
	mTabHost.addTab(mTabHost
		.newTabSpec("account")
		.setIndicator("Account",
			getResources().getDrawable(R.drawable.ic_info))
		.setContent(R.id.account));


	mTabHost.addTab(mTabHost
		.newTabSpec("tasks")
		.setIndicator("Tasks",
			getResources().getDrawable(R.drawable.ic_star))
		.setContent(R.id.taskList));

	new TasksGetTask().execute();

	mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
	    @Override
	    public void onTabChanged(String tabId) {
		new TasksGetTask().execute();
	    }
	});

	setButtonsByTracking();
    }


    public void onStartTracking(View view) {
	//	startService(Model.locationUpdateServiceIntent);


	if (((LocationManager) getApplicationContext().getSystemService(
		Context.LOCATION_SERVICE))
		.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

	    startService(new Intent(getApplicationContext(),
		    LocationUpdateService.class));

	    //			registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdateService.BROADCAST_ACTION));
	    Model.isTracking = true;
	    setButtonsByTracking();
	}

	else {
	    AlertDialog ad = new AlertDialog.Builder(this).create();
	    ad.setMessage("This function is available only with enabled GPS, please turn it on in your device's settings.");
	    ad.setButton(DialogInterface.BUTTON_NEUTRAL, "ok", (Message) null);
	    ad.show();
	}
    }

    public void onStopTracking(View view) {
	stopService(Model.locationUpdateServiceIntent);
	//		if (broadcastReceiver.) unregisterReceiver(broadcastReceiver);
	Model.isTracking = false;
	setButtonsByTracking();
    }


    private void setButtonsByTracking() {
	if (Model.isTracking) {
	    getButton(R.id.startTrackingButton).setVisibility(View.GONE);
	    getButton(R.id.stopTrackingButton).setVisibility(View.VISIBLE);
	} else {
	    getButton(R.id.startTrackingButton).setVisibility(View.VISIBLE);
	    getButton(R.id.stopTrackingButton).setVisibility(View.GONE);
	}
    }



    public void onLogoutClick(View view) {
	onStopTracking(view);
	new LogoutPostTask().execute();
    }

    public Button getButton(int id) {
	return (Button) findViewById(id);
    }

    private class LogoutPostTask extends
	    AsyncTask<String, Void, SaneHttpResponse> {
	@Override
	protected SaneHttpResponse doInBackground(String... str) {
	    return Model.getJson("session/logout.json");
	}

	@Override
	protected void onPostExecute(SaneHttpResponse response) {
	    Model.token = null;
	    Model.isLoggedIn = false;
	    loggedOut();

	}
    }


    private class TasksGetTask extends
	    AsyncTask<String, Void, SaneHttpResponse> {
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
		for (JSONObject task : response.getList())
		    Model.tasks.add(new TaskJson(task));
	    }
	    if (Model.tasks == null || Model.tasks.isEmpty())
		return;
	    ListView listView = (ListView) findViewById(R.id.taskList);
	    ArrayAdapter<TaskJson> adapter = new ArrayAdapter<TaskJson>(
		    MainActivity.this, android.R.layout.simple_list_item_1,
		    android.R.id.text1, Model.tasks);
	    listView.setAdapter(adapter);
	    listView.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		    Model.selectedTask = Model.tasks.get(position);
		    Intent i = new Intent(getApplicationContext(),
			    DetailActivity.class);
		    startActivity(i);
		}

	    });
	}
    }


}