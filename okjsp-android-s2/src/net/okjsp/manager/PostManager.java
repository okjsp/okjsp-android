package net.okjsp.manager;

import java.util.ArrayList;

import net.okjsp.Const;
import net.okjsp.data.Post;
import net.okjsp.provider.CacheProvider;
import net.okjsp.provider.DbConst;
import net.okjsp.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PostManager implements Const, DbConst {
	protected static PostManager mInstance = null;
	protected static final boolean DEBUG_LOG = true;
	protected static final boolean DEBUG_LOG_VERBOSE = true;

	protected Context mContext;
	
	public static PostManager getInstance(Context context) {
		if (mInstance == null) mInstance = new PostManager(context);
		
		return mInstance;
	}

	public PostManager(Context context) {
		mContext = context;
	}

    @SuppressWarnings("unused")
    public ArrayList<Post> loadPostList() {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	Cursor c = cr.query(CacheProvider.POST_URI, CacheProvider.TablePost.PROJECTION_ALL, 
    			null, null, FIELD_CREATED_AT + " DESC");
    	
    	if (DEBUG_LOG) Log.d("loadPostList(): " + c.getCount());
    	
    	ArrayList<Post> post_list = new ArrayList<Post>();   
    	
    	post_list.clear();
    	while(c.moveToNext()) {
    		Post post = new Post();
    		post.setId(c.getInt(c.getColumnIndex(FIELD_POST_ID)));
    		post.setUrl(c.getString(c.getColumnIndex(FIELD_POST_URL)));
    		post.setTitle(c.getString(c.getColumnIndex(FIELD_POST_TITLE)));
    		post.setBoardName(c.getString(c.getColumnIndex(FIELD_BOARD_DISPLAY_NAME)));
    		post.setBoardUrl(c.getString(c.getColumnIndex(FIELD_BOARD_URI_HOST)));
    		post.setWriterName(c.getString(c.getColumnIndex(FIELD_POST_WRITER_NAME)));
    		post.setProfileImageUrl(c.getString(c.getColumnIndex(FIELD_POST_WRITER_PHOTO_URL)));
    		post.setTimeStamp(c.getString(c.getColumnIndex(FIELD_POST_TIMESTAMP)));
    		post.setReadCount(c.getInt(c.getColumnIndex(FIELD_POST_CLICK_COUNT)));
    		post.setAsRead(c.getInt(c.getColumnIndex(FIELD_POST_ISREAD)) > 0 ? true : false);
    		
    		if (DEBUG_LOG && DEBUG_LOG_VERBOSE) {
    			Log.d("[" + post.getId() + "]: " + post.toString());
    		}
    		post_list.add(post);
    	}
    	c.close();

    	return post_list;
    }
    
    public void savePostList(ArrayList<Post> post_list) {
        for(Post post : post_list) {
        	savePost(post);
        }
    }
    
    @SuppressWarnings("unused")
    public void savePost(Post post) {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	
    	if (isExist(post.getId())) {
    		if (DEBUG_LOG && DEBUG_LOG_VERBOSE) Log.e("[" + post.getId() + "] " + post.getUrl() + ", " + Uri.parse(post.getUrl()).getLastPathSegment() + " exist!!!");
    		return;
    	}

    	ContentValues values = new ContentValues();
        values.clear();
        values.put(FIELD_POST_ID, post.getId());
        values.put(FIELD_POST_URL, post.getUrl());
        values.put(FIELD_POST_TITLE, post.getTitle());
        values.put(FIELD_BOARD_URI_HOST,post.getBoardUri());
        values.put(FIELD_BOARD_DISPLAY_NAME, post.getBoardName());
        values.put(FIELD_POST_CLICK_COUNT, post.getReadCount());
        values.put(FIELD_POST_WRITER_NAME, post.getWriterName());
        values.put(FIELD_POST_WRITER_PHOTO_URL, post.getProfileImageUrl());
        values.put(FIELD_POST_TIMESTAMP, post.getTimeStamp());
        values.put(FIELD_POST_ISREAD, post.isRead() ? 1 : 0);
        values.put(FIELD_CREATED_AT, post.getTime());
        values.put(FIELD_UPDATED_AT, System.currentTimeMillis());
        Uri uri = cr.insert(CacheProvider.POST_URI, values);
        
        if (DEBUG_LOG && DEBUG_LOG_VERBOSE) {
        	Log.v("savePost():" + uri.toString());
        }
    }    
    
    public boolean isExist(int post_id) {
    	boolean is_exist = false;

    	ContentResolver cr = getBaseContext().getContentResolver();
    	String where = FIELD_POST_ID + " = " + post_id;
    	Cursor c = cr.query(CacheProvider.POST_URI, CacheProvider.TablePost.PROJECTION_ALL, 
    			where, null, null);
    	
    	is_exist = (c.getCount() > 0) ? true : false;
    	
    	c.close();
    	return is_exist;
    }
    
    public void setAsRead(int post_id) {
    	ContentResolver cr = getBaseContext().getContentResolver();
    	String where = FIELD_POST_ID + " = " + post_id;
    	ContentValues cv = new ContentValues();
    	cv.put(FIELD_POST_ISREAD, 1);
    	cr.update(CacheProvider.POST_URI, cv, where, null);
    }
    
    protected Context getBaseContext() {
    	return mContext;
    }
	
}
