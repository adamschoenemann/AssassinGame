package aau.med3.assassin;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventListener;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

// TODO: Implement these methods to provide GCM communication
public class GCMIntentService extends GCMBaseIntentService {
	
	private final static String TAG = "GMCIntentService";
	
	public GCMIntentService(){
		super(Globals.SENDER_ID);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onError(Context ctx, String errorId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context ctx, Intent intent) {
		Log.d(TAG, "GCM Message Received!");
		if(Globals.user != null && Globals.user.loggedIn)
			Globals.user.syncFromServer();

	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		
		try {
			UserCRUD crud = new UserCRUD();
			User user = Globals.user;
			if(user == null)
				return;
			JSONObject data = new JSONObject();
			data.put("ID", user.ID);
			data.put("regId", regId);
			crud.update(data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	protected void onUnregistered(Context ctx, String regId) {
		// TODO Auto-generated method stub
		
	}

}
