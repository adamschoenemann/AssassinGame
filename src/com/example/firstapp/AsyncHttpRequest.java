package com.example.firstapp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncHttpRequest extends AsyncTask<String, String, String> {
	
	public Exception exception;
	public String protocol = "http";
	public String domain;
	public String encoding = "utf-8";
	public HashMap<String, String> params = new HashMap<String, String>();
	
	
	@Override
	protected String doInBackground(String... urls) {
		try {
//			URL url = new URL("http://10.0.0.2/android_test/user/create?username=from_android");
//			URL url = new URL(urls[0]);
			String base = protocol + "://" + domain + "?";
			String pars = "";
			
			Set<Entry<String, String>> set = params.entrySet();
			Iterator<Entry<String, String>> iter = set.iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
				pars += URLEncoder.encode(entry.getKey(), encoding) + "=" + URLEncoder.encode(entry.getValue(), encoding) + "&";
			}
			pars = pars.substring(0, pars.length() - 1);
			
			URL url = new URL(base + pars);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			Log.d("DEBUG", con.getResponseMessage());
			con.disconnect();
			return con.getResponseMessage();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(String result){
		super.onPostExecute(result);
		
		Log.d("DEBUG", result);
		
	}
	
}
