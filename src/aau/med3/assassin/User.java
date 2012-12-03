package aau.med3.assassin;

import java.net.ConnectException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

// TODO: Implement methods for reading and updating and deleting
public class User implements DataListener<String> {
	
	private String _url = "user/";
	public DataListener<JSONArray> listener;
	
	private AsyncHttpRequest setupRequest(String str){
		AsyncHttpRequest req = new AsyncHttpRequest();
		req.domain = Globals.SERVER_LOCATION + _url + str;		
		req.listener = this;
		return req;
	}
	
	
	public void create(UserData data){
		
		AsyncHttpRequest req = setupRequest("create");
		req.params = (HashMap<String, String>) data;
		req.execute("");
		
	}
	
	public void read(Integer ID){
		AsyncHttpRequest req = setupRequest("read");
		req.params.put("ID", ID.toString());
		req.execute("");
	}
	
	public void update(UserData data){
		AsyncHttpRequest req = setupRequest("update");
		req.params = (HashMap<String, String>) data;
		req.execute("");
		
	}
	
	public void delete(Integer ID){
		String url = _url + "delete";
	}

	@Override
	public void onDataComplete(String data){
		try {
			JSONArray json = new JSONArray(data.toString());
			listener.onDataComplete(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
