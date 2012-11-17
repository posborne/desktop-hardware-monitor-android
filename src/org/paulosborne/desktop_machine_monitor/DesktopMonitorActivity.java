package org.paulosborne.desktop_machine_monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB) public class DesktopMonitorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop_monitor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_desktop_monitor, menu);
        return true;
    }

    public void updateDisplay(View view) {
    	UpdateDesktopStaticsTask updateDesktopStatisticsTask = new UpdateDesktopStaticsTask();
    	updateDesktopStatisticsTask.execute(new String[] { "http://192.168.1.50:8080/api/statsdump" });
    }

    private class UpdateDesktopStaticsTask extends AsyncTask<String, Void, JSONObject> {
    	@Override
    	protected JSONObject doInBackground(String... urls) {
    		for (String url: urls) {
    			DefaultHttpClient client = new DefaultHttpClient();
    			HttpGet httpGet = new HttpGet(url);
    			try {
    				HttpResponse response = client.execute(httpGet);
    				InputStream content = response.getEntity().getContent();
    				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
    				StringBuilder sb = new StringBuilder();
    				String line;
    				while ((line = buffer.readLine()) != null) {
    					sb.append(line + "\n");
    				}
    				content.close();
    				JSONObject jsonObject = new JSONObject(sb.toString());
    				return jsonObject;
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(JSONObject jsonObject) {
    		System.out.println(jsonObject);
    		TextView cpuUsageTextView = (TextView)findViewById(R.id.cpuUsage);
    		ProgressBar cpuUsageProgressBar = (ProgressBar)findViewById(R.id.cpuProgressBar);
    		try {
				int cpuLoadPercent = jsonObject.getInt("cpu_load_percent");
	    		cpuUsageTextView.setText("" + cpuLoadPercent);
	    		cpuUsageProgressBar.setProgress(cpuLoadPercent);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
}
