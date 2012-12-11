package aau.med3.assassin.activities;

import aau.med3.assassin.R;
import aau.med3.assassin.R.layout;
import aau.med3.assassin.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class StatusActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status, menu);
		return true;
	}

}
