package net.okjsp.manager;

import java.io.IOException;
import java.util.ArrayList;

import net.okjsp.Const;
import net.okjsp.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;

public class NetworkManager implements Const {
	protected static final boolean DEBUG_LOG = true;
	
	protected Context mContext;

	protected static NetworkManager mInstance = null;
	protected static DefaultHttpClient mHttpClient = null;
	protected static CookieStore mCookieStore = null;

	protected static String mUsername;
	protected static String mPassword;
	
	public static NetworkManager getInstance(Context context) {
		if (mInstance == null) mInstance = new NetworkManager(context);
		
		return mInstance;
	}
	
	public NetworkManager(Context context) {
		mContext = context;
	}
	
    public static String login(String username, String password) {
        DefaultHttpClient httpclient = null;
        String data = null;

        mUsername = username;
        mPassword = password;
        
        Log.d("login(" + username + ")");
        if (mHttpClient == null) {
            Log.v("No available HttpClient!!");
            httpclient = new DefaultHttpClient();
        } else {
            httpclient = mHttpClient;
        }
        
        try {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair(USERNAME, username));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));

            HttpPost httpPost = new HttpPost(LOGIN_URL);
            UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            httpPost.setEntity(entityRequest);
            HttpResponse response = httpclient.execute(httpPost);
            
            HttpEntity entity = response.getEntity();
            Log.v("----------------------------------------");
            Log.v("" + response.getStatusLine());
            if (entity != null) {
                data = EntityUtils.toString(entity);
                Log.v("Response content length: " + entity.getContentLength() + ", " + data.length());
                //Log.e(TAG, "data:" + data);
            } else data = "";
            
            if (entity != null) {
                entity.consumeContent();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (isLoginSuccess(data)) {
            mHttpClient = httpclient;
            Log.v("Login success!!!");
            mCookieStore = mHttpClient.getCookieStore();
        } else {
            Log.v("Login failed!!!");
        }
        
        return data;
    }
   
    public static CookieStore getCookieStore() {
        return mCookieStore;
    }

    public static boolean isLoginSuccess(String data) {
        boolean success = false;
        return success;
    }

    public static DefaultHttpClient getHttpClientInstance() {
        return mHttpClient;
    }
	
}
