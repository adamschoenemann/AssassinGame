package aau.med3.assassin.activities;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.DataListener;
import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import aau.med3.assassin.SimpleSHA1;
import aau.med3.assassin.User;
import aau.med3.assassin.UserData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SignUpActivity extends Activity implements DataListener<JSONArray> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
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
	
	public void onSubmitClicked(View view) throws IOException{
		
		UserData usrData = new UserData();
		LinearLayout viewParent = (LinearLayout) findViewById(R.id.user_create_layout);
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
						// TODO Auto-generated catch block
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
		

		User usr = new User();
		
		usr.listener = this;
		usr.create(usrData);
		
				
	}
	
	// USER sends data of type JSONArray
	public void onDataComplete(JSONArray data){
		
		try {
			
			JSONArray json = data;
			JSONObject obj = json.getJSONObject(0);
			Log.d(Globals.DEBUG, "ID is: " +  obj.getString("ID"));
			SharedPreferences prefs = getSharedPreferences(Globals.PREF_FILENAME, MODE_PRIVATE);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("ID", obj.getInt("ID"));
			editor.putString("email", obj.getString("email"));
			editor.putString("first_name", obj.getString("first_name"));
			editor.putString("last_name", obj.getString("last_name"));
			editor.putInt("alive", obj.getInt("alive"));
			editor.putString("education", obj.getString("education"));
			editor.putString("password", obj.getString("password"));
			editor.putInt("target", obj.getInt("target"));
			editor.putString("phone_id", obj.getString("phone_id"));
			editor.putInt("point", obj.getInt("points"));
			
			
			editor.commit();
			Log.d(Globals.DEBUG, "[0]: " + json.getJSONObject(0).toString());
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Display Success Alert
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
