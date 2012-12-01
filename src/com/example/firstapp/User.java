package com.example.firstapp;

import java.util.HashMap;


public class User implements DataListener {
	
	private String _url = "user/";
	public DataListener listener;
	
	public void create(UserData data){
		String url = _url + "create";
		AsyncHttpRequest req = new AsyncHttpRequest();
		req.domain = Globals.serverLocation + url;		
		req.params = (HashMap<String, String>) data;
		req.listener = this;
		req.execute("");
		
		/* COPY USERDATA TO PARAMS
		Set<Entry<String, String>> set = params.entrySet();
		Iterator<Entry<String, String>> iter = set.iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
			pars += URLEncoder.encode(entry.getKey(), encoding) + "=" + URLEncoder.encode(entry.getValue(), encoding) + "&";
		}
		*/
	}
	
	public void read(Integer ID){
		String url= _url + "read";
	}
	
	public void update(UserData data){
		String url = _url + "update";
	}
	
	public void delete(Integer ID){
		String url = _url + "delete";
	}

	@Override
	public void onDataComplete(String data) {
		
		listener.onDataComplete(data);
	}
}
