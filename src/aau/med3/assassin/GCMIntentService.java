package aau.med3.assassin;



import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.activities.DashboardActivity;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

// TODO: Implement these methods to provide GCM communication
public class GCMIntentService extends GCMBaseIntentService {
	
	private final static String TAG = "GMCIntentService";
	protected static final Integer NOTIFICATION_ID = 12;
	
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
		String msg = intent.getStringExtra("message");
		Log.d(TAG, "GCM Message Received! Message: " + msg);
		
		if(msg.equals("KILL")){
			Log.d(TAG, "Message is KILL");
			if(Globals.user != null){
				Log.d(TAG, "Sending ACTION_DIED brodcast");
				sendOrderedBroadcast(new Intent(Globals.ACTION_DIED),
						null,
						new BroadcastReceiver() {
							
							@Override
							public void onReceive(Context context, Intent intent) {
								Log.d(TAG, "ACTION DIED broadcast finished with result code: " + String.valueOf(getResultCode()));
								if(getResultCode() == Activity.RESULT_OK){ // No Interceptions
									Context ctx = getApplicationContext();
									Intent resultIntent = new Intent(GCMIntentService.this, DashboardActivity.class);
									NotificationManager notMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
									
									NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
									.setDefaults(Notification.DEFAULT_SOUND)
									.setSmallIcon(R.drawable.ic_launcher)
									.setContentTitle("You Died!")
									.setContentText("Somebody killed you!");

									TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
									stackBuilder.addParentStack(DashboardActivity.class);
									stackBuilder.addNextIntent(resultIntent);
									
									PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
									builder.setContentIntent(resultPendingIntent);
									
									
									notMan.notify(NOTIFICATION_ID, builder.build());
								} else {
									// Do nothing
								}
								
								
							}
						},
						null,
						Activity.RESULT_OK,
						null,
						null);
			}
		}
		if(Globals.user != null && Globals.user.loggedIn){
			Globals.user.syncFromServer();
		}
			

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
