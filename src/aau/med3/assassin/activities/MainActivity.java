package aau.med3.assassin.activities;


import aau.med3.assassin.AssassinService;
import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_main);
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	checkServiceStatus();
    	if(Globals.assassinService != null && !Globals.assassinService.hasEventListener(Event.STATE_CHANGED, new ServiceStateChangedListener())){
    		Globals.assassinService.addEventListener(Event.STATE_CHANGED, new ServiceStateChangedListener());
    	}
    		
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	AssassinService service = Globals.assassinService;
    	if(service != null && service.hasEventListener(Event.STATE_CHANGED, new ServiceStateChangedListener())){
    		service.removeEventListener(Event.STATE_CHANGED, new ServiceStateChangedListener());
    	}
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
    
    public void btn_game_onclick(View view){
    	if(Globals.assassinService != null && Globals.assassinService.running){
    		Intent intent = new Intent(this, GameActivity.class);
        	startActivity(intent);
    	}
    	
    }
    
    private void checkServiceStatus(){
    	AssassinService service = Globals.assassinService;
    	if(service != null && service.running){
    		findViewById(R.id.btn_game).setVisibility(View.VISIBLE);
    	}
    	else {
    		findViewById(R.id.btn_game).setVisibility(View.GONE);
    	}
    	
    	Log.d(Globals.DEBUG, "checkServiceStatus()");
    }
    
    private class ServiceStateChangedListener implements EventListener {

		@Override
		public void handle(Event evt) {
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					Log.d(Globals.DEBUG, "Running checkServiceStatus from EventListener");
					checkServiceStatus();
				}
				
			});
			
		}
    	
    }
}
