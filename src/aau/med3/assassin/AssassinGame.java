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
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		Integer ID = prefs.getInt("ID", 0);
		if(ID != null && (ID.equals("") == false)){
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
		try {
			
			
			JSONObject obj = userData;
			Log.d(Globals.DEBUG, "ID is: " +  obj.getString("ID"));
			SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(DB.users.ID, obj.getInt("ID"));
			editor.putString(DB.users.email, obj.getString("email"));
			editor.putInt(DB.users.alive, obj.getInt("alive"));
			editor.putString(DB.users.education, obj.getString("education"));
			editor.putString(DB.users.password, obj.getString("password"));
			editor.putInt(DB.users.target_ID, obj.getInt("target_ID"));
			editor.putString(DB.users.phone_ID, obj.getString("phone_ID"));
			editor.putInt(DB.users.points, obj.getInt("points"));
			editor.putString(DB.users.MAC, obj.getString("MAC"));
			
			
			editor.commit();
			Log.d(Globals.DEBUG, "Logged in with: " + obj.toString());
			
			// Start assassin service
			startAssassinService();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startAssassinService(){
		Intent serviceIntent = new Intent(AssassinService.class.getName());
		startService(serviceIntent);
	}
}
