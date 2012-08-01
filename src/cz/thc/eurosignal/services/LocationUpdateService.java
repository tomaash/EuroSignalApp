package cz.thc.eurosignal.services;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import cz.thc.eurosignal.model.Model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationUpdateService extends Service {
//	private Handler mHandler = new Handler();
//	public static final int DELAY = 1000;
	public static final String BROADCAST_ACTION = "cz.sw.android.services.broadcastaction";
	
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
	public void onCreate() {
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Intent intent = new Intent(BROADCAST_ACTION);
		sendBroadcast(intent);
		startListener();
		new LocationPostTask().execute("1","2","3");
	}

	@Override
	public void onDestroy() {
		Intent intent = new Intent(BROADCAST_ACTION);
		sendBroadcast(intent);
		stopListener();
	}
	
	private LocationManager locationManager;
	private LocationListener locationListener;

	private Double lat;
	private Double lng;
	private Float acc;

	
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
			return Model.postJson("locations.json",jsonData).status;
		}

		@Override
		protected void onPostExecute(String status) {
			Log.e("post status",status);
			Intent intent = new Intent(BROADCAST_ACTION);
			sendBroadcast(intent);
		}
	}

	public void startListener(){
		Log.e("listener start","hello");
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {

				lat = location.getLatitude();
				lng = location.getLongitude();
				acc = location.getAccuracy();

				Log.e("lat",""+lat);
				Log.e("lng",""+lng);
				Log.e("acc",""+acc);

				new LocationPostTask().execute(lat.toString(),lng.toString(),acc.toString());

				//				if (acc<100) {
				//					Log.e("loc good enough",""+acc);
				//					stopListener();
				//				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				Log.e("loc status",""+status);
			}

			public void onProviderEnabled(String provider) {

				Log.e("loc enable",provider);
			}

			public void onProviderDisabled(String provider) {
				Log.e("loc disable",provider);
			}
		};

		// Register the listener with the Location Manager to receive location updates
//		Criteria criteria = new Criteria();
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setAltitudeRequired(false);
//		criteria.setBearingRequired(false);
//		criteria.setSpeedRequired(false);
		//		criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
		//		criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
//		locationManager.requestLocationUpdates(30000,10.0f,criteria,locationListener,null);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,10.0f,locationListener);
//		locationManager.requestLocationUpdates(10000,0.f,criteria,locationListener,null);
	}

	public void stopListener(){
		Log.e("listener stop","goodbye");
		locationManager.removeUpdates(locationListener);
	}
	
	
}
