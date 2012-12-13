package aau.med3.assassin.activities;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import aau.med3.assassin.AssassinGame;
import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import aau.med3.assassin.SimpleSHA1;
import aau.med3.assassin.CRUD.UserCRUD;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class SignUpActivity extends Activity {

	private ScrollView signUpLayout;
	private LinearLayout statusLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		signUpLayout = (ScrollView) findViewById(R.id.signup_layout_container);
		statusLayout = (LinearLayout) findViewById(R.id.signup_status);
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	
	public void onSubmitClicked(View view){
		attemptLogin();
	}
	
	public void attemptLogin(){
		TextView pwdForm = (TextView) findViewById(R.id.form_pwd);
		TextView emailForm = (TextView) findViewById(R.id.form_email);
		View focusView = null;
		
		pwdForm.setError(null);
		emailForm.setError(null);
		
		String pwd = pwdForm.getText().toString();
		String email = emailForm.getText().toString();
		
		Boolean cancel = false;
		if(TextUtils.isEmpty(email) || (!email.contains("@"))){
			cancel = true;
			emailForm.setError("Email must contain @");
			focusView = emailForm;
		}
		if(pwd.length() < 6){
			cancel = true;
			pwdForm.setError("Password must be at least six characters long");
			focusView = pwdForm;
		}
		
		if(cancel == true){
			if(focusView != null) focusView.requestFocus();
		} else {
			createNewUser();
		}
	}
	
	public void createNewUser() {
		
		JSONObject usrData;
		try {
			usrData = new JSONObject();
			LinearLayout viewParent = (LinearLayout) findViewById(R.id.signup_layout);
			int count = viewParent.getChildCount();
			
			// Collect form data
			for(int i = 0; i < count; i++){
				View v = (View) viewParent.getChildAt(i);
				
				if(v.getTag() == null){
					continue;
				}
				
				if(v instanceof TextView){
					if(v.getId() == R.id.form_pwd){ // If password field
						
						String plainpw = ((TextView) v).getText().toString();
						String sha1;
						try {
							sha1 = SimpleSHA1.SHA1(plainpw);
							Log.d(Globals.DEBUG, "Encrypted pwd: " + sha1);
							usrData.put(v.getTag().toString(), sha1);
							
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						
					} else {
						usrData.put(v.getTag().toString(), ((TextView) v).getText().toString());
					}
					
				}
				else if(v instanceof Spinner){
					usrData.put(v.getTag().toString(), ((Spinner) v).getSelectedItem().toString());;
				}
			}
			
			// Get BT MAC address
			BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
			String MAC; 
			if(bta != null){
				MAC = bta.getAddress();
			} else {
				Log.d(Globals.DEBUG, "No bluetooth adapter found");
				MAC = "00-00-00-00-00-00";
			}
			
			usrData.put("MAC", MAC);
			
			// Get GCM regID
			String regId = GCMRegistrar.getRegistrationId(this);
			if(regId.equals("")){
				GCMRegistrar.register(this, Globals.SENDER_ID);
				regId = GCMRegistrar.getRegistrationId(this);
			}
			usrData.put("regId", regId);
			
			UserCRUD usr = new UserCRUD();
			usr.addEventListener(Event.SUCCESS, new RequestSuccessHandler());
			usr.create(usrData);

			
		} catch (JSONException e){
			e.printStackTrace();
		}

		
		
				
	}
	
	public class RequestSuccessHandler implements EventListener {
		public void handle(Event evt){
			
			AssassinGame app = (AssassinGame) getApplication();
			try {
				JSONArray data = (JSONArray) evt.data;
				app.login(data.getJSONObject(0));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// Display Success Alert
			AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
			builder.setTitle("User Registration")
				.setMessage("User succesfully created")
				.setCancelable(false)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int id){
						dialog.cancel();
					}
				});
			
			AlertDialog dialog = builder.create();
			
			dialog.show();
		}
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sign_in, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
