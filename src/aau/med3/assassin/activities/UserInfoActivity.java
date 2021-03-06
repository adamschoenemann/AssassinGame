package aau.med3.assassin.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import aau.med3.assassin.User;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserInfoActivity extends Activity {
	
	ListView listView;
	ArrayAdapter<String> adapter;
	ArrayList<String> items = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		// Show the Up button in the action bar.
		setupActionBar();

		
		
	}
	
	@Override
	public void onStart(){
		super.onStart();
		listView = (ListView) findViewById(R.id.list_user_info);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);
		listView.setAdapter(adapter);

		
		HashMap<String, Object> map = new HashMap<String, Object>();
		User user = Globals.user;
		if(user != null){
			map.put("ID", user.ID);
			map.put("Email", user.email);
			map.put("MAC", user.MAC);
			map.put("Target MAC", user.target_MAC);
			map.put("Points", user.points);
			map.put("Alive", user.alive);
			map.put("Target Name", user.target_name);
			
			for (Map.Entry<String, ?> entry : map.entrySet()){
				String key = entry.getKey();
				String val = entry.getValue().toString();
				items.add(key + ": " + val);
			}
		
		}
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_user_info, menu);
		return true;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
