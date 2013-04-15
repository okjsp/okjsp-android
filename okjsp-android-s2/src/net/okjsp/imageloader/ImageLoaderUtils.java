/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.okjsp.imageloader;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * Class containing some static utility methods.
 */
public class ImageLoaderUtils {
	public static final String TAG = "ImageLoaderUtils";
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    public static File getExternalCacheDir(Context context) {
    	File external_cache = null;
        if (hasExternalCacheDir()) {
        	external_cache = context.getExternalCacheDir();
        	if (external_cache != null) return external_cache;
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName();
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static void makeExternalCacheDir(Context context) {
        String ext_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        ext_path += ("/Android/data/" + context.getPackageName());
        File dir = new File (ext_path);
        boolean success = dir.mkdirs();
        if (!dir.exists() && !success) {
            Log.e(TAG, "Can't create base directory: " + dir.getPath());
        }
    }
    
    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressLint("NewApi")
    public static long getUsableSpace(File path) {
    	long usable_space = 0L;
    	
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        	// Log.e(TAG, "getUsableSpace(" + path.getAbsolutePath() + "):" + path.getUsableSpace());
        	usable_space = path.getUsableSpace();
        	// Some devices return '0' value even if there are enough spaces
        	if (usable_space > 0L) return usable_space;
        }
        
        /*
         * https://code.google.com/p/ad-away/issues/detail?id=104
         *  Issue 104:	java.lang.IllegalArgumentException at android.os.StatFs.native_setup(Native Method)
         *  
         * http://stackoverflow.com/questions/14796931/illegalargumentexception-in-statfs-in-webviewcore-internal-thread
         * 
         */
        try {
	        final StatFs stats = new StatFs(path.getPath());
	        usable_space = (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        	
        	String ext_path = Environment.getExternalStorageDirectory().getPath();
        	if (path.getPath().startsWith(ext_path)) {
                StatFs stat = new StatFs(ext_path);
                long blockSize = stat.getBlockSize();
                //long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                //mSdSize.setSummary(formatSize(totalBlocks * blockSize));
                //mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);        	

                usable_space = availableBlocks * blockSize;
        	}
        }
        
        return usable_space;
    }

    /**
     * Get the memory class of this device (approx. per-app memory limit)
     *
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     *
     * @return
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if ActionBar is available.
     *
     * @return
     */
    public static boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
