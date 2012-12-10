package aau.med3.assassin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventListener;
import android.content.SharedPreferences;
import android.util.Log;

public class User {

	public String email, MAC, target_MAC; 
	public Integer ID, points;
	public Boolean loggedIn;
	public SharedPreferences prefs;
	
	public User(SharedPreferences prefs){
		this.prefs = prefs;
		loggedIn = false;
	}
	
	public void fromJSON(JSONObject json){
		try {
			email = json.getString("email");
			MAC = json.getString("MAC");
			target_MAC = json.getString("target_MAC");
			ID = json.getInt("ID");
			points = json.getInt("points");
			loggedIn = true;
			
		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("email", email);
		json.put("MAC", MAC);
		json.put("target_MAC", target_MAC);
		json.put("ID", ID);
		json.put("points", points);
		
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
			editor.commit();
		}
	}
	
	public void load(){

		String defValue = "";
		email = prefs.getString("email", defValue);
		MAC = prefs.getString(MAC, defValue);
		target_MAC = prefs.getString("target_MAC", defValue);
		ID = prefs.getInt("ID", 0);
		points = prefs.getInt("points", 0);
		
		loggedIn = true;
	}
	
	public void kill(String MAC){
		UserCRUD userCRUD = new UserCRUD();
		userCRUD.addEventListener(Event.SUCCESS, new EventListener(){

			@Override
			public void handle(Event evt){
				try {
					JSONArray data = (JSONArray) evt.data;
					JSONObject json = data.getJSONObject(0);
					if(true){
						target_MAC = json.getString("target_MAC");
						 points++;
						
						Log.d(Globals.DEBUG, "Target " + json.getString("ID") + "sucessfully killed");
					}
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
					fromJSON(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		crud.read(ID);
		
	}
	
	public void syncToServer() {
		
		try {
			JSONObject json;
			json = toJSON();
			UserCRUD crud = new UserCRUD();
			crud.addEventListener(Event.SUCCESS, new EventListener(){

				@Override
				public void handle(Event e) {
					Log.d(Globals.DEBUG, "Used synchronized to server");
					
				}
				
			});
			crud.update(json);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
}
