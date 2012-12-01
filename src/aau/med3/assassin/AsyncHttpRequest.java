package aau.med3.assassin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	public DataListener listener;
	public HashMap<String, String> params = new HashMap<String, String>();
	
	
	protected String readStream(InputStream in){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while((line = reader.readLine()) != null){
				sb.append(line);
			}
			
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@Override
	protected String doInBackground(String... urls){
		try {

			String base = protocol + "://" + domain + "?";
			String pars = "";
			
			// Iterate through hashmap
			Set<Entry<String, String>> set = params.entrySet();
			Iterator<Entry<String, String>> iter = set.iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
				pars += URLEncoder.encode(entry.getKey(), encoding) + "=" + URLEncoder.encode(entry.getValue(), encoding) + "&";
			}
			pars = pars.substring(0, pars.length() - 1);
			
			URL url = new URL(base + pars);
			Log.d("DEBUG", "URL: " + url.toExternalForm());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			String ret = readStream(con.getInputStream());
			
			Log.d("DEBUG", con.getResponseMessage());
			con.disconnect();
			
			
			return ret;
		} catch (MalformedURLException e) {

			Log.d("DEBUG", e.toString());
//			e.printStackTrace();
		} catch (IOException e) {

			Log.d("DEBUG", e.toString());
		}
		return "";
	}
	
	@Override
	protected void onPostExecute(String result){
		super.onPostExecute(result);
		if(listener != null){
			listener.onDataComplete(result);
		}
				
		
	}
	
}
