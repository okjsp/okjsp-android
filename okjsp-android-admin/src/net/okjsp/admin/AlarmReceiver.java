package net.okjsp.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver implements Const {
	protected static final String TAG = "AlarmReceiver";
	protected static int mCount = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive():" + mCount);

    	Intent service = new Intent(context, OkjspPollingService.class);
    	service.setAction(INTENT_ACTION_CHECK);
    	context.startService(service);
	}
}

