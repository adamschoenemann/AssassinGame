package aau.med3.assassin;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class AssassinGame extends Application {
	
	
	
	@Override
	public void onCreate(){
		Log.d(Globals.DEBUG, "Game started!");
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		Integer ID = prefs.getInt("ID", 0);
		Log.d(Globals.DEBUG, "I HAS STARTEEED!!!!!");
		Log.d(Globals.DEBUG, "User_ID: " + ID.toString());
		
		try {
			DB.userCols = new JSONObject("{\"ID\" : \"ID\", \"email\", \"email\", \"password\", \"password\", \"phone_ID\", \"phone_ID\", \"target_ID\", \"target_ID\"}");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
