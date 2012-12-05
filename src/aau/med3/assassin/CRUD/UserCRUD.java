package aau.med3.assassin.CRUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aau.med3.assassin.AsyncHttpRequest;
import aau.med3.assassin.DB;
import aau.med3.assassin.DataListener;
import aau.med3.assassin.Globals;
import aau.med3.assassin.ServerInfo;

// TODO: Implement methods for reading and updating and deleting
public class UserCRUD implements DataListener<String> {
	
	private String _url = "user/";
	public DataListener<JSONArray> listener;
	
	private AsyncHttpRequest setupRequest(String str){
		AsyncHttpRequest req = new AsyncHttpRequest();
		req.domain = ServerInfo.LOCATION + _url + str;		
		req.listener = this;
		return req;
	}
	
	
	public void create(JSONObject data){
		
		AsyncHttpRequest req = setupRequest("create");
		req.params = data;
		req.execute("");
		
	}
	
	public void read(Integer ID){
		AsyncHttpRequest req = setupRequest("read");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put(DB.userCols.getString("ID"), ID);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	public void read(String email){
		AsyncHttpRequest req = setupRequest("read");
		JSONObject json;
		try {
			json = new JSONObject();
			json.put(DB.userCols.getString("email"), email);
			req.params = json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		req.execute("");
	}
	
	public void update(JSONObject data){
		AsyncHttpRequest req = setupRequest("update");
		req.params = data;
		req.execute("");
		
	}
	
	public void delete(Integer ID){
		String url = _url + "delete";
	}

	@Override
	public void onDataComplete(String data){
		try {
			JSONArray json = new JSONArray(data.toString());
			if(listener != null) listener.onDataComplete(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
