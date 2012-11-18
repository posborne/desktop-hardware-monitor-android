package org.paulosborne.desktop_machine_monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB) public class DesktopMonitorActivity extends Activity {

	/* Constants */
	private static final int THREADPOOL_THREADS = 5;
	private static final int CHECK_SECONDS = 5;
	private static final String STATSDUMP_URL = "http://192.168.1.50:8080/api/statsdump";

	private ScheduledExecutorService scheduledTaskExecutor;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop_monitor);
        scheduledTaskExecutor = Executors.newScheduledThreadPool(THREADPOOL_THREADS);
        scheduledTaskExecutor.scheduleAtFixedRate(new Runnable() {
        	public void run() {
        		updateStatisticsAndView();
        	}
        }, 0, CHECK_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_desktop_monitor, menu);
        return true;
    }

    /*
     * Go to the REST webserver and grab the latest statistics and update the UI
     */
    public void updateStatisticsAndView() {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(STATSDUMP_URL);
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
			final JSONObject jsonObject = new JSONObject(sb.toString());
			runOnUiThread(new Runnable() {
				public void run() {
		    		TextView cpuUsageTextView = (TextView)findViewById(R.id.cpuUsage);
		    		ProgressBar cpuUsageProgressBar = (ProgressBar)findViewById(R.id.cpuProgressBar);
		    		TextView ramFreeTextView = (TextView)findViewById(R.id.ramFreeTextView);
		    		ProgressBar ramFreeProgressBar = (ProgressBar)findViewById(R.id.ramFreeProgressBar);
		    		TextView cpuTemperatureTextView = (TextView)findViewById(R.id.cpuTemperatureTextView);
		    		try {
						int cpuLoadPercent = jsonObject.getInt("cpu_load_percent");
						double cpuTempCelsius = jsonObject.getDouble("cpu_temp_celsius");
						long totalMemory = jsonObject.getLong("total_memory");
						long freeMemory = jsonObject.getLong("free_memory");
			    		double ramFreePercent = ((double)freeMemory / totalMemory);
						cpuUsageTextView.setText("" + cpuLoadPercent);
			    		cpuUsageProgressBar.setProgress(cpuLoadPercent);
			    		DecimalFormat df = new DecimalFormat("#.##");
			    		ramFreeTextView.setText("" + df.format(freeMemory / 1024.0 / 1024.0) + " MB (Total: " + df.format(totalMemory / 1024.0 / 1024.0) + ")");
			    		ramFreeProgressBar.setProgress((int)(100 * ramFreePercent));
			    		cpuTemperatureTextView.setText("CPU Temp (Celsius): " + cpuTempCelsius);
			    		cpuTemperatureTextView.setBackgroundColor(Color.GREEN);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
    
}
