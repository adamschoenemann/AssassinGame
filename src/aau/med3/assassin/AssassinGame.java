package aau.med3.assassin;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AssassinGame extends Application {
	
	
	
	@Override
	public void onCreate(){
		Log.d(Globals.DEBUG, "Game started!");
		// Check if user data is saved
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		Integer ID = prefs.getInt("ID", 0);
		
		// If it is, initialize user and start assassin service
		if(ID != null && (ID.equals(0) == false)){
			Globals.user = new User(getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE));
			Globals.user.load();
			startAssassinService();
		}
		Log.d(Globals.DEBUG, "User_ID: " + ID.toString());
		
		try {
			DB.userCols = new JSONObject("{\"ID\" : \"ID\", \"email\":\"email\", \"password\": \"password\", \"phone_ID\" : \"phone_ID\", \"target_ID\" : \"target_ID\", \"education\": \"education\" }");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void login(JSONObject userData){

			
		Globals.user = new User(getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE));
		Globals.user.fromJSON(userData);
		Globals.user.save();
		// Start assassin service
		startAssassinService();

	}
	
	public void startAssassinService(){
		Intent serviceIntent = new Intent(AssassinService.class.getName());
		startService(serviceIntent);
	}
	
}
