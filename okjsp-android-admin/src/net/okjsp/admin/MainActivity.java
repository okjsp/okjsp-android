package net.okjsp.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements Const {
	protected final static String TAG = "MainActivity";
	protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (getIntent() != null && "net.okjsp.ACTION_NOTIFICATION_CLICKED".equals(getIntent().getAction())) {
            AlertDialog.Builder builder = getAlertDialogBuilder(this);
            builder.setTitle(R.string.monitoring_stop)
            .setMessage(R.string.monitoring_stop_msg)
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
		        	Log.d(TAG, "Alarm canceled!");
		        	stopMonitoringService();
		        	finish();
				}
			})
			.setNegativeButton(android.R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
			        finish();
			        overridePendingTransition(0, R.anim.zoom_exit);
				}
			})
            .show();
        	return;
        }
        startMonitoringService();

		
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
		        finish();
		        overridePendingTransition(0, R.anim.zoom_exit);
			}
		}, 1500);
    }
    
    @Override
    protected void onNewIntent (Intent intent) {
    	super.onNewIntent(intent);
    	Log.d(TAG, "onNewIntent: " + (intent != null ? intent.toString() : ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void startMonitoringService() {
    	Intent intent = new Intent(this, OkjspPollingService.class);
    	intent.setAction(INTENT_ACTION_START);
    	startService(intent);
    }
    
    public void stopMonitoringService() {
    	Intent intent = new Intent(this, OkjspPollingService.class);
    	intent.setAction(INTENT_ACTION_STOP);
    	startService(intent);
    }
    
    public AlertDialog.Builder getAlertDialogBuilder(Context context) {
        return ((android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) ?
                new AlertDialog.Builder(context) :
                new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK));        
    }
}
