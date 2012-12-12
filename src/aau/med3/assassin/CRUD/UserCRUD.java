package aau.med3.assassin.CRUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.AsyncHttpRequest;
import aau.med3.assassin.DB;
import aau.med3.assassin.Globals;
import aau.med3.assassin.ServerInfo;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventHandler;
import aau.med3.assassin.events.EventListener;
import android.util.Log;

// TODO: Implement methods for reading and updating and deleting
public class UserCRUD extends EventDispatcher {
	
	private String _url = "user/";
	private final static String TAG = "UserCRUD";
	
	private AsyncHttpRequest setupRequest(String str){
		AsyncHttpRequest req = new AsyncHttpRequest();
		req.domain = ServerInfo.LOCATION + _url + str;		
		
		req.addEventListener(Event.SUCCESS, new RequestSuccessHandler());
		req.addEventListener(Event.FAILURE, new RequestFailedHandler());
		return req;
	}
	
	
	public void create(JSONObject data){
		
		AsyncHttpRequest req = setupRequest("create");
		req.params = data;
		req.execute("");
		
	}
	
	public void read(Integer ID){
		AsyncHttpRequest req = setupRequest("read");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put("ID", ID);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	public void read(String email){
		AsyncHttpRequest req = setupRequest("read");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put("email", email);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	public void readWhere(String field, String value){
		AsyncHttpRequest req = setupRequest("read");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put(field, value);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	public void update(JSONObject data){
		AsyncHttpRequest req = setupRequest("update");
		req.params = data;
		req.execute("");
		
	}
	
	public void delete(Integer ID){
		String url = _url + "delete";
	}
	
	public void kill(String MAC){
		AsyncHttpRequest req = setupRequest("kill");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put("MAC", MAC);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	private class RequestSuccessHandler implements EventListener {

		@Override
		public void handle(Event e) {
			String result = (String) e.data;
			try {
				JSONArray json = new JSONArray(result);
				dispatchEvent(Event.SUCCESS, json);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(Globals.DEBUG, "Event " + e.name + "dispatched. Data: " + result);
		}
		
	}
	
	private class RequestFailedHandler implements EventListener {

		@Override
		public void handle(Event e) {
			UserCRUD.this.dispatchEvent(Event.FAILURE, null);
			Log.d(TAG, "Request failed");
		}
		
	}

}
