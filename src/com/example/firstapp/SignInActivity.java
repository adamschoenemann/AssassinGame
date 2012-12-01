package com.example.firstapp;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SignInActivity extends Activity implements DataListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		
		
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
		
		for(int i = 0; i < count; i++){
			View v = (View) viewParent.getChildAt(i);
			
			if(v.getTag() == null){
				continue;
			}
			
			if(v instanceof TextView){
				usrData.put(v.getTag().toString(), ((TextView) v).getText().toString());
			}
			else if(v instanceof Spinner){
				usrData.put(v.getTag().toString(), ((Spinner) v).getSelectedItem().toString());;
			}
		}
		

		User usr = new User();
		
		usr.listener = this;
		usr.create(usrData);
		
		/*
		req.listener = this;
		req.domain = "10.0.2.2/android_test/user/read";
		req.params.put("ID", "8");
		req.params.put("first_name", firstName);
		req.params.put("last_name", lastName);

		req.execute("hey");

		*/
	}
	
	public void onDataComplete(String data){
		Log.d("DEBUG", "Returned data: " + data);
		try {
			
			JSONArray json = new JSONArray(data);
			Log.d("DEBUG", "[0]: " + json.getJSONObject(0).toString());
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
