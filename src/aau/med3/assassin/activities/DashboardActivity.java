package aau.med3.assassin.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.AssassinGame;
import aau.med3.assassin.AssassinService;
import aau.med3.assassin.Globals;
import aau.med3.assassin.KillAction;
import aau.med3.assassin.R;
import aau.med3.assassin.StateTracker;
import aau.med3.assassin.User;
import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

// TODO: Rename to dashboard
public class DashboardActivity extends Activity {
	
	private final static String TAG = "StatusActivity";
	
	private CheckBox cbLoggedIn;
	private CheckBox cbAlive;
	private CheckBox cbBTEnabled;
	private CheckBox cbBTDiscoverable;
	private CheckBox cbNetworkEnabled;
	private Button btnLogIn;
	private Button btnLogOut;
	private Button btnKill;
	private Button btnInfo;
	private Button btnSync;
	private View viewLoading;
	private TextView loadMsg;
	
	private KillAction killAction;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action + "broadcast received!");
			
			if(action.equals(Globals.ACTION_REFRESH)){
				refresh();
			}
			if(action.equals(Globals.ACTION_LOGGED_IN)){
				Globals.user.addEventListener(User.UPDATED, new EventListener() {
					
					@Override
					public void handle(Event e) {
						refresh();
						
					}
				});
			}
		}
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		cbLoggedIn = (CheckBox) findViewById(R.id.dash_cb_logged_in);
		cbAlive = (CheckBox) findViewById(R.id.dash_cb_alive);
		cbBTEnabled = (CheckBox) findViewById(R.id.dash_cb_bt);
		cbBTDiscoverable = (CheckBox) findViewById(R.id.dash_cb_disc);
		cbNetworkEnabled = (CheckBox) findViewById(R.id.dash_cb_network);
		btnLogIn = (Button) findViewById(R.id.dash_btn_log_in);
		btnLogOut = (Button) findViewById(R.id.dash_btn_log_out);
		btnKill = (Button) findViewById(R.id.dash_btn_kill);
		btnInfo = (Button) findViewById(R.id.dash_btn_info);
		btnSync = (Button) findViewById(R.id.dash_btn_sync);
		viewLoading = (View) findViewById(R.id.dash_layout_loading);
		loadMsg = (TextView) findViewById(R.id.dash_loading_msg);
		
		btnLogIn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
				startActivity(intent);
			}
			
		});
		
		btnLogOut.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				((AssassinGame) getApplication()).logOut();
				refresh();
				Toast.makeText(DashboardActivity.this, "Successfully loged out.", Toast.LENGTH_SHORT).show();
			}
			
		});
		
		btnKill.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBtnKillPressed();
				
			}
		});
		
		btnInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(DashboardActivity.this, UserInfoActivity.class));
				
			}
		});
		
		btnSync.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				User user = Globals.user;
				if(user != null && user.loggedIn == true){
					showProgress("Synchronizing...");
					user.addEventListener(Event.SUCCESS, new SyncHandler());
					user.addEventListener(Event.FAILURE, new SyncHandler());
					user.synchronize();
				}
				
			}
			
			class SyncHandler implements EventListener {

				@Override
				public void handle(Event e) {
					e.target.removeEventListener(Event.SUCCESS, new SyncHandler());
					e.target.removeEventListener(Event.FAILURE, new SyncHandler());
					hideProgress();
					refresh();
					String msg = (e.name == Event.SUCCESS) ? "Synchronized!" : "Network Error! Synchronization failed";
					Toast.makeText(DashboardActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Globals.ACTION_REFRESH);
		filter.addAction(Globals.ACTION_LOGGED_IN);
		filter.addAction(Globals.ACTION_LOGGED_OUT);
		registerReceiver(receiver, filter);
		
		bootstrap();
	}
	
	protected void onBtnKillPressed() {
		killAction = new KillAction(this, Globals.user.target_MAC);
		if(killAction.bootstrap() == false){
			Toast.makeText(this, "Somethings not right. Please refresh..", Toast.LENGTH_LONG).show();
			return;
		}
		// Bootstrap successful
		AssassinService service = Globals.assassinService;
		if(service == null || service.running == false){
			Toast.makeText(this, "Somethings not right. Please refresh..", Toast.LENGTH_LONG).show();
			return;
		}
		
		
		showProgress("Scanning for target...");
		
		// Pause AssassinService
		service.addEventListener(AssassinService.PAUSED, new EventListener() {
			
			@Override
			public void handle(Event e) {
				e.target.removeEventListener(AssassinService.PAUSED, this);
				Log.d(TAG, "AssassinService paused while scanning!");
				setupKillEventListeners(killAction);
				killAction.initKill();
				
			}
		});
		service.pauseScanning();
		

	}
	
	private void setupKillEventListeners(KillAction action){
		action.addEventListener(KillAction.FINISHED, new EventListener(){

			@Override
			public void handle(Event e) {
				String msg = (String) e.data;
				if(msg.equals(KillAction.SUCCESS)){
					Globals.user.kill(Globals.user.target_MAC);
					Toast.makeText(DashboardActivity.this, "Target succesfully killed!", Toast.LENGTH_SHORT).show();
				}
				if(msg.equals(KillAction.TARGET_NOT_FOUND)){
					Log.d(TAG, "Target not found");
					Toast.makeText(DashboardActivity.this, "Target not found!", Toast.LENGTH_SHORT).show();
				}
				
				if(killAction != null){
					killAction.removeAllEventListeners();
					killAction = null;
					Log.d(TAG, "KillAction nullified");
				}
				if(Globals.assassinService != null){
					Globals.assassinService.startScanning();
				}
				hideProgress();
			}
			
		});
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		refresh();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	}

	
	public void refresh(){
		viewLoading.setVisibility(View.VISIBLE);
		loadMsg.setText("Refreshing ...");
		
		StateTracker sm = new StateTracker(this);
		cbNetworkEnabled.setChecked( (sm.isNetworkConnected()) ? true : false);
		cbBTEnabled.setChecked( (sm.isBTEnabled()) ? true : false);
		cbBTDiscoverable.setChecked( (sm.isBTDiscoverable()) ? true : false);
		
		User user = Globals.user;
		if(user == null){
			btnLogIn.setEnabled(true);
			btnLogIn.setVisibility(View.VISIBLE);
			
			btnLogOut.setVisibility(View.GONE);
			btnLogOut.setEnabled(false);
			
			cbLoggedIn.setChecked(false);
			cbAlive.setChecked(false);
		} else {
			if(user.loggedIn == true){
				btnLogIn.setVisibility(View.GONE);
				btnLogIn.setEnabled(false);
				
				btnLogOut.setVisibility(View.VISIBLE);
				btnLogOut.setEnabled(true);
				
				cbLoggedIn.setChecked(true);
				if(user.alive){
					cbAlive.setChecked(true);
				} else {
					cbAlive.setChecked(false);
				}
			}
		}
		
		
		Boolean allChecked = (cbNetworkEnabled.isChecked() && cbBTDiscoverable.isChecked() && cbLoggedIn.isChecked() && cbAlive.isChecked()) ;
		if(allChecked){

			btnKill.setEnabled(true);
		} else {

			btnKill.setEnabled(false);
		}
		
		viewLoading.setVisibility(View.GONE);
		loadMsg.setText("");
	}
	
	public void bootstrap(){
		SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		StateTracker sm = new StateTracker(this);
		
		Log.d(TAG, "email: " + email + " password: " + password);
		
		
		if(!email.equals("") && !password.equals("") && sm.isNetworkConnected()){
			try {
				
				showProgress("Logging in " + email);
				Log.d(TAG, "Loggin in " + email);
				
				UserCRUD crud = new UserCRUD();
								
				crud.addEventListener(Event.SUCCESS, new EventListener(){

					@Override
					public void handle(Event evt) {
						try {
							JSONObject json = ((JSONArray) evt.data).getJSONObject(0);
														
							((AssassinGame) getApplication()).login(json);
							Log.d(TAG, "User succesfully logged in with ID: " + json.getString("ID"));
							
							viewLoading.setVisibility(View.GONE);
							loadMsg.setText("");
							
							Toast.makeText(DashboardActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
							refresh();
						} catch (JSONException e) {
							Toast.makeText(DashboardActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						
					}
					
				});
				
				crud.read(email);

			} catch (Exception e){
				e.printStackTrace();
			} finally {
				hideProgress();
			}
			
		}
		else {
			Log.d(TAG, "No persistent user data found");
		}
	}
	
	public void showProgress(String msg){
		viewLoading.setVisibility(View.VISIBLE);
		loadMsg.setText(msg);
	}
	
	public void hideProgress(){
		viewLoading.setVisibility(View.GONE);
		loadMsg.setText("");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_dashboard, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.dash_opt_refresh:
				refresh();
				return true;
		
			default:
				return super.onOptionsItemSelected(item);
		}
		
		
	}
}
