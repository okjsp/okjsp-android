package net.okjsp.util;

import android.os.Build;

public class Utils {
    public static boolean checkApiLevel(int level) {
        return (Build.VERSION.SDK_INT >= level);
    }
}
