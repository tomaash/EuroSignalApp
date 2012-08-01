package cz.thc.eurosignal.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskJson {
	
	public TaskJson(JSONObject data){
		this.data = data;
	}
	
	public JSONObject data;
	
	public String toString() {
		try {
			return this.data.getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			return "N/A";
		}
	}
	
	public String get(String key) {
		try {
			return data.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public String get(String key, String key2) {
		try {
			JSONObject o1 = data.getJSONObject(key);
			return o1.getString(key2);
		} catch (JSONException e) {
			return null;
		}
	}

	
	
	
}
