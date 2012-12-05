package aau.med3.assassin.activities;


import aau.med3.assassin.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
                
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void btn_sign_up_onclick(View view){
    	Intent intent = new Intent(this, SignUpActivity.class);
    	startActivity(intent);
    }
    
    public void btn_log_in_onclick(View view){
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }
    
    public void btn_user_info_onclick(View view){
    	Intent intent = new Intent(this, UserInfoActivity.class);
    	startActivity(intent);
    }
}
