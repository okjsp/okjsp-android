package net.okjsp.admin.utils;

import net.okjsp.admin.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    public static final int TYPE_INFO = -1;
    public static final int TYPE_WARNING = -2;
    
    public static View getToastView(Context context, String msg, int icon_id) {
        LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toast_view = li.inflate(R.layout.custom_toast, null);
        ImageView iv_icon = (ImageView)toast_view.findViewById(R.id.iv_icon);
        TextView tv_msg = (TextView)toast_view.findViewById(R.id.tv_msg);
        iv_icon.setImageResource(icon_id);
        tv_msg.setText(msg);
        
        return toast_view;
    }

    public static View getToastView(Context context, String msg) {
        return getToastView(context, msg, R.drawable.ic_launcher);
    }
    
    public static Toast makeText(Context context, String msg, int icon_id, int duration) {
        switch (icon_id) {
            case TYPE_INFO:
            	// TODO: need to change icon for indicating informations
                icon_id = R.drawable.ic_launcher;
                break;
            case TYPE_WARNING:
            	// TODO: need to change icon for indicating warnings
                icon_id = R.drawable.ic_launcher;
                break;
            default:
                icon_id = R.drawable.ic_launcher;
                break;
        }

        View view = getToastView(context, msg, icon_id);
        
        Toast toast = new Toast(context);              
        toast.setDuration(duration);
        toast.setView(view);
        return toast;
    }    
    
    public static Toast makeText(Context context, int msg_id, int duration) {
        return makeText(context, context.getString(msg_id), R.drawable.ic_launcher, duration);
    }  
}
