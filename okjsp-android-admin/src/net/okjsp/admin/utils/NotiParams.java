
package net.okjsp.admin.utils;

import android.content.Intent;

public class NotiParams {
    public Intent intent = null;
    public String title = null;
    public String msg = null;
    public String tickerText = null;
    public boolean autocancel = false;
    public boolean first = false;
    public boolean ongoing = false;
    public boolean inprogress = false;
    public boolean alert = false;
    public boolean indeterminate = false;
    public int small_icon_id = -1;
    public int large_icon_id = -1;
    public int progress = 0;
    
    public NotiParams() {
    }
    
    public NotiParams(Intent intent, String title, String msg, String ticker) {
        this.intent = intent;
        this.title = title;
        this.msg = msg;
        this.tickerText = ticker;
    }
}
