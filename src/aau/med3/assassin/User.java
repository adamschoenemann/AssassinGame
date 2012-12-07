package aau.med3.assassin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import android.content.SharedPreferences;
import android.util.Log;

public class User {

	public String email, MAC, target_MAC; 
	public Integer ID;
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
			loggedIn = true;
			
		} catch (JSONException e){
			e.printStackTrace();
		}
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
			editor.commit();
		}
	}
	
	public void load(){

		String defValue = "";
		email = prefs.getString("email", defValue);
		MAC = prefs.getString(MAC, defValue);
		target_MAC = prefs.getString("target_MAC", defValue);
		ID = prefs.getInt("ID", 0);
		loggedIn = true;
	}
	
	public void kill(String MAC){
		UserCRUD userCRUD = new UserCRUD();
		userCRUD.onResponseListener = new EventListener<JSONArray>(){

			@Override
			public void onEvent(JSONArray data) {
				try {
					JSONObject json = data.getJSONObject(0);
					if(true){
						target_MAC = json.getString("target_MAC");
						// points++;
						
						Log.d(Globals.DEBUG, "Target " + json.getString("ID") + "sucessfully killed");
					}
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				
			}
			
		};
		userCRUD.kill(MAC);
	}
	
	
}
