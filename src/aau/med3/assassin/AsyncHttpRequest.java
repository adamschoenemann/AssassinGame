package aau.med3.assassin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncHttpRequest extends AsyncTask<String, String, String> {
	
	public Exception exception;
	public String protocol = "http";
	public String domain;
	public String encoding = "utf-8";
	public DataListener<String> listener;
	public JSONObject params;
//	public HashMap<String, String> params = new HashMap<String, String>();
	
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
			
			// Create params
			Iterator<?> keys = params.keys();
			while(keys.hasNext()){
				String key = (String) keys.next();
				String value = params.getString(key);
				pars += URLEncoder.encode(key, encoding) + "=" + URLEncoder.encode(value, encoding) + "&";
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
			exception = e;
			e.printStackTrace();
			cancel(true);
		} catch (IOException e) {
			exception = e;
			e.printStackTrace();
			cancel(true);
		} catch (JSONException e) {
			exception = e;
			e.printStackTrace();
			cancel(true);
		}
		return "";
	}
	
	@Override
	protected void onPostExecute(String result){
		super.onPostExecute(result);
		
		if(listener != null){
			try {
				listener.onDataComplete(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
