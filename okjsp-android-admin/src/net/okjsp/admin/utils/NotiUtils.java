package net.okjsp.admin.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotiUtils {
    protected final Context mContext;
    protected final NotificationManager mNotiManager;
    protected final NotiParams mParams;
    protected PendingIntent mPendingIntent;
    protected boolean mIsProgressBar = false;

    public NotiUtils(Context context) {
        mContext = context;
        mNotiManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mParams = new NotiParams();
    }
    
    public NotiUtils(Context context, NotiParams params) {
        mContext = context;
        mNotiManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (params == null) {
            mParams = new NotiParams();
        } else  {
            mParams = params;
        }
    }
    
    public void cancel(int id) {
        mNotiManager.cancel(id);
    }
    
    public NotiParams getNotiParams() {
        return mParams;
    }
    
    public NotiUtils setIntent(Intent intent) {
        mParams.intent = intent;
        return this;
    }
    
    public NotiUtils setIndeterminate(boolean indeterminate) {
        mIsProgressBar = true;
        mParams.indeterminate = indeterminate;
        return this;
    }
    
    public NotiUtils setSmallIcon(int icon_id) {
        mParams.small_icon_id = icon_id;
        return this;
    }
    
    public NotiUtils setLargeIcon(int icon_id) {
        mParams.large_icon_id = icon_id;
        return this;
    }
    
    public NotiUtils setTitle(String title) {
        mParams.title = title;
        return this;
    }
    
    public NotiUtils setMessage(String msg) {
        mParams.msg = msg;
        return this;
    }
    
    public NotiUtils setTickerMessage(String msg) {
        mParams.tickerText = msg;
        return this;
    }
    
    public NotiUtils setProgress(int progress) {
        mParams.progress = progress;
        return this;
    }

    public NotiUtils setOngoing(boolean ongoing) {
        mParams.ongoing = ongoing;
        return this;
    }
    
    public void notify(int noti_id, String title, String msg, String ticker, int progress) {
        setTitle(title);
        setMessage(msg);
        setTickerMessage(ticker);
        setProgress(progress);
        Notification noti = getNotification(mContext);
        mNotiManager.notify(noti_id, noti);
    }

    @SuppressWarnings("deprecation")
    protected Notification getNotification(Context context) {
        Notification noti = null;

        if (mParams != null) {
            mPendingIntent = PendingIntent.getActivity(context, 0, mParams.intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        Resources res = context.getResources();

        if (checkApiLevel(11)) {
            Bitmap mIconBitmap = BitmapFactory.decodeResource(res, mParams.large_icon_id);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentIntent(mPendingIntent)
                    .setOngoing(mParams.ongoing)
                    .setSmallIcon(mParams.small_icon_id)
                    .setLargeIcon(mIconBitmap)
                    .setAutoCancel(mParams.autocancel)
                    .setContentTitle(mParams.title)
                    .setContentText(mParams.msg);

            if (mParams.first) {
                builder.setWhen(System.currentTimeMillis());
                if (checkApiLevel(14) && mIsProgressBar) {
                    builder.setProgress(100, 0, false);
                }
            } else if (mParams.progress >= 0) {
                if (checkApiLevel(14) && mIsProgressBar) {
                    builder.setProgress(100, mParams.progress, false);
                }
            }

            if (mParams.tickerText != null) {
                builder.setTicker(mParams.tickerText);
            }

            if (mParams.alert) {
                builder.setDefaults(Notification.DEFAULT_ALL);
            }

            if (mIconBitmap != null) {
                builder.setLargeIcon(mIconBitmap);
            }

            if (checkApiLevel(16)) {
                noti = builder.build();
            } else {
                noti = builder.getNotification();
            }
        } else {
            Bitmap mIconBitmap = BitmapFactory.decodeResource(res, mParams.small_icon_id);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentIntent(mPendingIntent)
                    .setOngoing(mParams.ongoing)
                    .setSmallIcon(mParams.small_icon_id)
                    .setLargeIcon(mIconBitmap)
                    .setAutoCancel(mParams.autocancel)
                    .setContentTitle(mParams.title)
                    .setContentText(mParams.msg);

            if (mParams.first) {
                builder.setWhen(System.currentTimeMillis());
                if (mIsProgressBar) builder.setProgress(100, 0, false);
            } else if (mParams.progress >= 0) {
                if (mIsProgressBar) builder.setProgress(100, mParams.progress, false);
            }

            if (mParams.tickerText != null) {
                builder.setTicker(mParams.tickerText);
            }

            if (mParams.alert) {
                builder.setDefaults(Notification.DEFAULT_ALL);
            }

            if (mIconBitmap != null) {
                builder.setLargeIcon(mIconBitmap);
            }

            noti = builder.build();
        }

        return noti;
    }
    
    public boolean checkApiLevel(int level) {
        return (Build.VERSION.SDK_INT >= level);
    }
}