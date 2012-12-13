package aau.med3.assassin;

import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

public class AssassinGame extends Application {
	
	
	
	@Override
	public void onCreate(){
		Log.d(Globals.DEBUG, "Game started!");
		/*
		// Check if user data is saved
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		Integer ID = prefs.getInt("ID", 0);
//		
		// If it is, initialize user and start assassin service
		if(ID != null && (ID.equals(0) == false)){
			Globals.user = new User(getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE));
			Globals.user.load();
			startAssassinService();
		}
		Log.d(Globals.DEBUG, "User_ID: " + ID.toString());
		*/

	}
	
	public void login(JSONObject userData){

		try {	
		Globals.user = new User(getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE));
		Globals.user.fromJSON(userData);
		Globals.user.save();
		
		GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        
        if(regId.equals(userData.getString("regId")) == false){
        	GCMRegistrar.register(this, Globals.SENDER_ID);

        } else {
        	Log.d(Globals.DEBUG, "Already registered");
        }
		
        Globals.events.dispatchEvent(User.LOGGED_IN, null);
        
		// Start assassin service
		startAssassinService();
		} catch (JSONException e){
			e.printStackTrace();
		}

	}
	
	public void logOut(){
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
		Globals.events.dispatchEvent(User.LOGGED_OUT, null);
		Globals.user = null;
		
		
	}
	
	
	public void startAssassinService(){
		Log.d(Globals.DEBUG, "Attempting to start AssassinService");
		Intent serviceIntent = new Intent(AssassinService.class.getName());
		startService(serviceIntent);
	}
	
}
