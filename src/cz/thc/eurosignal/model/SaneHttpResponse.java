package cz.thc.eurosignal.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class SaneHttpResponse {
	public String body;
	public String status;
	public int code;
	
	public SaneHttpResponse(int code,String status,String body) {
		super();
		this.body = body;
		this.status = status;
		this.code = code;
		
	}
	
	public JSONObject getJsonObject(){
		try {
			return (JSONObject) new JSONTokener(body).nextValue();
		} catch (Exception e) {
			Log.e("JSON","parse error");
			return null;
		}		
	}
	
	public JSONArray getJsonArray(){
		try {
			return (JSONArray) new JSONTokener(body).nextValue();
		} catch (Exception e) {
			Log.e("JSON","parse error");
			return null;
		}		
	}
	
	public List<JSONObject> getList(){
		try {
			JSONArray array = (JSONArray) new JSONTokener(body).nextValue();
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			int i;
			for (i=0;i<array.length();i++){
				list.add(array.getJSONObject(i));
			}
			return list;			
		} catch (Exception e) {
			Log.e("JSON","parse error");
			return null;
		}				
	}
	
	
	
	public SaneHttpResponse(){
		super();
	}
}
