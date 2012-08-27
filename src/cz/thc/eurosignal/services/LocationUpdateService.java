package cz.thc.eurosignal.services;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cz.thc.eurosignal.model.Model;

public class LocationUpdateService extends Service {
    //	private Handler mHandler = new Handler();
    //	public static final int DELAY = 1000;
    public static final String BROADCAST_ACTION = "cz.sw.android.services.broadcastaction";

    private static final String TAG = LocationUpdateService.class
	    .getSimpleName();
    private static final int LOCATION_INTERVAL = 30000;
    private static final float LOCATION_DISTANCE = 0f;

    //	private Runnable periodicTask = new Runnable() {
    //		public void run() {
    //			mHandler.postDelayed(periodicTask, DELAY);
    //			Intent intent = new Intent(BROADCAST_ACTION);
    //			sendBroadcast(intent);
    //		}
    //	};

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Log.i(TAG, "onStartCommand");
	super.onStartCommand(intent, flags, startId);
	return START_STICKY;
    }

    private void initializeLocationManager() {
	Log.i(TAG, "initializeLocationManager");

	if (locationManager == null)
	    locationManager = (LocationManager) getApplicationContext()
		    .getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onCreate() {
	initializeLocationManager();
	Intent intent = new Intent(BROADCAST_ACTION);
	sendBroadcast(intent);
	startListener();
	new LocationPostTask().execute("1", "2", "3");
    }

    @Override
    public void onDestroy() {
	Intent intent = new Intent(BROADCAST_ACTION);
	sendBroadcast(intent);
	stopListener();
    }

    private LocationManager locationManager;
    private LocationListener locationListener;




    public void startListener() {
	Log.i("listener start", "listener start");
	locationListener = new EurosignalLocationListener();
	try {
	    locationManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL,
		    LOCATION_DISTANCE, locationListener);
	} catch (java.lang.SecurityException ex) {
	    Log.i(TAG, "fail to request location update, ignore", ex);
	} catch (IllegalArgumentException ex) {
	    Log.d(TAG, "gps provider does not exist " + ex.getMessage());
	}
    }

    public void stopListener() {
	Log.i("listener stop", "goodbye");
	if (locationManager != null)
	    try {
		locationManager.removeUpdates(locationListener);
	    } catch (Exception ex) {
		Log.i(TAG, "fail to remove location listners, ignore", ex);
	    }

    }


    private class EurosignalLocationListener implements LocationListener {
	@Override
	public void onLocationChanged(Location location) {

	    Double lat;
	    Double lng;
	    Float acc;

	    lat = location.getLatitude();
	    lng = location.getLongitude();
	    acc = location.getAccuracy();

	    Log.i("lat", "lat: " + lat + " lon: " + lng + " acc: " + acc);

	    new LocationPostTask().execute(lat.toString(), lng.toString(),
		    acc.toString());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    Log.i("loc status", "" + status);
	}

	@Override
	public void onProviderEnabled(String provider) {

	    Log.i("loc enable", provider);
	}

	@Override
	public void onProviderDisabled(String provider) {
	    Log.i("loc disable", provider);
	}
    }


    private class LocationPostTask extends AsyncTask<String, Void, String> {
	@Override
	protected String doInBackground(String... str) {
	    JSONObject jsonData = new JSONObject();
	    try {
		jsonData.put("lat", str[0]);
		jsonData.put("lng", str[1]);
		jsonData.put("acc", str[2]);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    return Model.postJson("locations.json", jsonData).status;
	}

	@Override
	protected void onPostExecute(String status) {
	    Log.i("post status", status);
	    Intent intent = new Intent(BROADCAST_ACTION);
	    sendBroadcast(intent);
	}
    }
}
