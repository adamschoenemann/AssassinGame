package com.example.firstapp;

import java.io.IOException;
import java.net.URLEncoder;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SignInActivity extends Activity {

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
		AsyncHttpRequest req = new AsyncHttpRequest();
//		String url = "http://10.0.2.2/android_test/user/create?";
//		String encoding = "utf-8";
		TextView tv = (TextView) findViewById(R.id.form_firstName);
		String firstName = tv.getText().toString();
		tv = (TextView) findViewById(R.id.form_lastName);
		String lastName = tv.getText().toString();
//		String userName = "android_user";
//		url += "username=" + userName + "&first_name=" + firstName + "&last_name=" + lastName;
//		Log.d("DEBUG", url);
		
		req.domain = "10.0.2.2/android_test/user/create?";
		req.params.put("first_name", firstName);
		req.params.put("last_name", lastName);
//		req.execute("http://10.0.2.2/android_test/user/create?username=from_android");
		req.execute("hey");


	}

}
