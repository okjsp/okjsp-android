package net.okjsp.admin;

import net.okjsp.admin.utils.NotiUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.util.Log;

public class OkjspPollingService extends IntentService implements Const {
	protected static final String TAG = "OkjspPollingService";
	protected static final long ALARM_DURATION_MILLIS = 10 * DateUtils.MINUTE_IN_MILLIS; 
	
	protected static NotiUtils mNotiUtils;

	public OkjspPollingService() {
		super("OkjspPollingService");
	}
	
	public OkjspPollingService(String name) {
		super(name);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) return;
		
		if (INTENT_ACTION_START.equals(intent.getAction())) {
			showNotification();
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			PendingIntent sender = getAlarmPendingIntent();
			am.setRepeating(AlarmManager.ELAPSED_REALTIME, ALARM_DURATION_MILLIS, ALARM_DURATION_MILLIS, sender);
		} else if (INTENT_ACTION_STOP.equals(intent.getAction())) {
			hideNotification();
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			PendingIntent sender = getAlarmPendingIntent();
			am.cancel(sender);
		} else if (INTENT_ACTION_CHECK.equals(intent.getAction())) {
			(new NetCheckThread()).start();
		}
	}
	
	protected PendingIntent getAlarmPendingIntent() {
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
		
		return sender;
	}
	
	protected void showNotification() {
        mNotiUtils = new NotiUtils(getBaseContext());
        Intent intent_param = new Intent(getBaseContext(), MainActivity.class);
        intent_param.setAction("net.okjsp.ACTION_NOTIFICATION_CLICKED");
        mNotiUtils.setIntent(intent_param);
        mNotiUtils.setSmallIcon(R.drawable.ic_launcher);
        mNotiUtils.setLargeIcon(R.drawable.ic_launcher);
        mNotiUtils.setOngoing(true);
        String msg = getBaseContext().getString(R.string.monitoring);
        mNotiUtils.notify(NOTIFICATION_ID_DEFAULT, getString(R.string.app_name), msg, msg, 0);
	}

	protected void hideNotification() {
    	if (mNotiUtils != null) mNotiUtils.cancel(0);
	}
	
	protected void vibrate() {
		Vibrator vib = (Vibrator)getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
		vib.vibrate(700);
	}
	
    protected Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		// Log.d(TAG, "handleMessage()");
    		switch(msg.what) {
    		case NETWORK_STATUS_CODE:
    			mNotiUtils.notify(NOTIFICATION_ID_DEFAULT, getBaseContext().getString(R.string.monitoring), getBaseContext().getString(R.string.status_code) + msg.arg1, null, 0);
    			if (msg.arg1 != HttpStatus.SC_OK) {
    				vibrate();
    			}
    			break;
    		case NETWORK_ERR_EXCEPTION:
    			Exception e = (Exception)msg.obj;
    			mNotiUtils.notify(NOTIFICATION_ID_DEFAULT, getBaseContext().getString(R.string.monitoring), e.getMessage(), null, 0);
				vibrate();
    			break;
    		}
    	}
    };
	
	protected class NetCheckThread extends Thread {
		public void run() {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(new HttpGet(BASE_URL));
				// Log.d(TAG, "STATUS CODE: " + response.getStatusLine().getStatusCode());
				Message.obtain(mHandler, NETWORK_STATUS_CODE, response.getStatusLine().getStatusCode(), 0).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
				Message.obtain(mHandler, NETWORK_ERR_EXCEPTION, 0, 0, e).sendToTarget();
			}
		}
	}

}
