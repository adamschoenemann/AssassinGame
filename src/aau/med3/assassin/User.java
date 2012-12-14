package aau.med3.assassin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListenerProxy;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventListener;
import android.content.SharedPreferences;
import android.util.Log;

public class User extends EventDispatcher {

	private static final String TAG = "User";

	public static final String 	UPDATED = "userUpdated",
									LOGGED_IN = "userLoggedIn",
									LOGGED_OUT = "userLoggedOut";
	
	public Timestamp timestamp;
	public String email, MAC, target_MAC, password; 
	public Integer ID, points;
	public Boolean loggedIn, alive;
	public SharedPreferences prefs;
	
	private class UserUpdatedHandler implements EventListener {

		@Override
		public void handle(Event e) {
			timestamp = new Timestamp(new Date().getTime());
			
		}
		
	}
	
	public User(SharedPreferences prefs){
		this.prefs = prefs;
		loggedIn = false;
		addEventListener(UPDATED, new UserUpdatedHandler());
	}
	
	public void fromJSON(JSONObject json){
		try {
			email = json.getString("email");
			password = json.getString("password");
			MAC = json.getString("MAC");
			target_MAC = json.getString("target_MAC");
			ID = json.getInt("ID");
			points = json.getInt("points");
			timestamp = timestampFromString(json.getString("timestamp"));
			alive = (json.getInt("alive") == 1) ? true : false;
			loggedIn = true;
			dispatchEvent(UPDATED, null);
			dispatchEvent(LOGGED_IN, null);
			
		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("email", email);
		json.put("password", password);
		json.put("MAC", MAC);
		json.put("target_MAC", target_MAC);
		json.put("ID", ID);
		json.put("points", points);
		json.put("alive", (alive) ? 1 : 0);
		
		return json;
		
	}
	
	public void save(){
		if(loggedIn){
			SharedPreferences.Editor editor = prefs.edit();
			editor.clear();
			editor.commit();
			
			editor.putString("email", email);
			editor.putString("MAC", MAC);
			editor.putString("target_MAC", target_MAC);
			editor.putInt("ID", ID);
			editor.putInt("points", points);
			editor.putString("password", password);
			editor.commit();
			Log.d(TAG, "User saved!");
		}
	}
	
	
	public void kill(String MAC){
		UserCRUD userCRUD = new UserCRUD();
		userCRUD.addEventListener(Event.SUCCESS, new EventListener(){

			@Override
			public void handle(Event evt){
				try {
					JSONArray data = (JSONArray) evt.data;
					JSONObject json = data.getJSONObject(0);
					
					target_MAC = json.getString("target_MAC");
					points++;
					
					dispatchEvent(UPDATED, null);
					syncToServer(false);
					Log.d(Globals.DEBUG, "Target " + json.getString("ID") + "sucessfully killed");
					
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				
			}
			
		});
		userCRUD.kill(MAC);
	}
	
	public void syncFromServer(){
		UserCRUD crud = new UserCRUD();
		crud.addEventListener(Event.SUCCESS, new EventListener(){

			@Override
			public void handle(Event evt) {
				try {
					JSONObject json = ((JSONArray) evt.data).getJSONObject(0);
					fromJSON(json); // Dispatches UPDATED
					User.this.dispatchEvent(Event.SUCCESS, null);
					Log.d(TAG, "Synced from server");
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		crud.read(ID);
		
	}
	
	public void syncToServer(){
		syncToServer(true);
	}
	
	public void syncToServer(Boolean push) {
		
		try {
			JSONObject json;
			json = toJSON();
			if(push == false){
				json.put("push", 0);
			}
			UserCRUD crud = new UserCRUD();
			crud.addEventListener(Event.SUCCESS, new EventListener(){

				@Override
				public void handle(Event e) {
					Log.d(Globals.DEBUG, "User synchronized to server");
					User.this.dispatchEvent(Event.SUCCESS, null);
				}
				
			});
			crud.update(json);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public void synchronize(){
		
		UserCRUD crud = new UserCRUD();
		crud.addEventListener(Event.SUCCESS, new EventListener() {
			
			@Override
			public void handle(Event evt) {
				try {
					JSONObject data = ((JSONArray) evt.data).getJSONObject(0);
					Timestamp stamp = timestampFromString(data.getString("timestamp"));
					if(stamp.after(timestamp)){ // Server info is newer
						syncFromServer();
					} else {
						syncToServer(false);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		crud.addEventListener(Event.FAILURE, new EventListener() {
			
			@Override
			public void handle(Event e) {
				User.this.dispatchEvent(Event.FAILURE, null);				
			}
		});
		
		crud.read(ID);
	}
	
	private Timestamp timestampFromString(String in){

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			Date date = sdf.parse(in);
			Timestamp stamp = new Timestamp(date.getTime());
			return stamp;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
