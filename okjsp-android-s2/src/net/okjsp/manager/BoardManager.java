package net.okjsp.manager;

import java.util.ArrayList;

import net.okjsp.Const;
import net.okjsp.R;
import net.okjsp.data.Board;
import net.okjsp.provider.DbConst;
import net.okjsp.provider.CacheProvider;
import net.okjsp.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class BoardManager implements Const, DbConst {
	protected static BoardManager mInstance = null;
	protected static final boolean DEBUG_LOG = true;

	protected Context mContext;
	protected ArrayList<Board> mBoardList = new ArrayList<Board>();

	public static BoardManager getInstance(Context context) {
		if (mInstance == null) mInstance = new BoardManager(context);
		
		return mInstance;
	}
	
	public BoardManager(Context context) {
		mContext = context;
		
		if (loadBoardList() < 1) {
			initBoardList();
		}
		
		if (DEBUG_LOG) {
			/*int count = 0;
			for(Board board : mBoardList) {
				Log.d("[" + count++ + "]:" + board.getTitle() + ", " + board.getClickCount());
			}*/
		}
	}
	
	public ArrayList<Board> getBoardList() {
		return mBoardList;
	}
	
	public void initAnchorList() {
        final Resources res = mContext.getResources();
        String titles[] = res.getStringArray(R.array.anchor_names);
        String uris[] = res.getStringArray(R.array.anchor_links);

        for(int i = 0; i < titles.length; i++) {
        	Board board = new Board(titles[i], uris[i]);
        	mBoardList.add(board);
        }
	}
	
	public void initBoardList() {
        ContentResolver cr = mContext.getContentResolver();
        final Resources res = mContext.getResources();
        String titles[] = res.getStringArray(R.array.actions_names);
        String uris[] = res.getStringArray(R.array.actions_links);
        
        initAnchorList();
        
        for(int i = 0; i < titles.length; i++) {
        	ContentValues cv = new ContentValues();
        	Uri uri = Uri.parse(uris[i]);
        	cv.put(FIELD_BOARD_URI_HOST, uri.getHost());
        	cv.put(FIELD_BOARD_DISPLAY_NAME, titles[i]);
        	cv.put(FIELD_BOARD_INDEX, i);
        	cv.put(FIELD_CREATED_AT, System.currentTimeMillis());
        	
        	cr.insert(CacheProvider.BOARD_URI, cv);
        	
        	Board board = new Board(titles[i], uris[i]);
        	mBoardList.add(board);
        }
	}
	
	public int loadBoardList() {
		int count = 0;
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(CacheProvider.BOARD_URI, CacheProvider.TableBoard.PROJECTION_ALL, null, null, 
        		FIELD_BOARD_CLICK_COUNT + " DESC"
        		+ ", " + FIELD_BOARD_TIMESTAMP + " DESC"
        		+ ", " + FIELD_BOARD_INDEX + " ASC");

        count = c.getCount();
        if (DEBUG_LOG) {
        	Log.d("board count in DB: " + c.getCount());
        }
        
        if (count > 0) {
        	initAnchorList();
        }
        
        while(c.moveToNext()) {
        	if (DEBUG_LOG) {
        		Log.d("name:" + c.getString(c.getColumnIndex(FIELD_BOARD_URI_HOST))
        				+ ", clicked:" + c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)));
        	}
        	Board board = new Board(c.getString(c.getColumnIndex(FIELD_BOARD_DISPLAY_NAME)),
        			BOARD_URI_SCHEME + c.getString(c.getColumnIndex(FIELD_BOARD_URI_HOST)));
        	board.setClickCount(c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)));
        	board.setTimeStamp(c.getLong(c.getColumnIndex(FIELD_BOARD_TIMESTAMP)));
        	mBoardList.add(board);
        }
        
        c.close();
        
        return count;
	}
	
	public String getBoardName(String board_uri) {
		String board_name = null;
		String uri_host = null;
		
		if (TextUtils.isEmpty(board_uri)) {
			Log.e("getBoardName() called with null parameter!!!");
			return null;
		}
		
        final Resources res = mContext.getResources();
        String titles[] = res.getStringArray(R.array.anchor_names);
        String uris[] = res.getStringArray(R.array.anchor_links);
        for(int i = 0; i < uris.length; i++) {
        	if (board_uri.equals(Uri.parse(uris[i]).getHost())) {
        		return titles[i];
        	}
        }
		
		if (board_uri.startsWith(BOARD_URI_SCHEME)) {
			uri_host = Uri.parse(board_uri).getHost();
		} else {
			uri_host = board_uri;
		}
				
        ContentResolver cr = mContext.getContentResolver();
        String where = FIELD_BOARD_URI_HOST + " = '" + uri_host + "'";
        Cursor c = cr.query(CacheProvider.BOARD_URI, CacheProvider.TableBoard.PROJECTION_ALL, where, null, null);
        
        int count = c.getCount();
        if (DEBUG_LOG) {
        	Log.d("board count in DB: " + c.getCount() + ", " + board_uri);
        }
        
        if (count > 0) {
        	initAnchorList();
        }
        
        while(c.moveToNext()) {
        	board_name = c.getString(c.getColumnIndex(FIELD_BOARD_DISPLAY_NAME));
        	if (!TextUtils.isEmpty(board_name)) break;
        }
        c.close();
        
        if (DEBUG_LOG) Log.d("getBoardName(" + board_uri + "): " + board_name);
        
        return board_name;
	}
	
	public void onBoadClicked(String board) {
		if ("notice".equals(board) || "recent".equals(board)) return;
		
        ContentResolver cr = mContext.getContentResolver();
        String where = FIELD_BOARD_URI_HOST + " = '" + board + "'";
        Cursor c = cr.query(CacheProvider.BOARD_URI, CacheProvider.TableBoard.PROJECTION_ALL, 
        		where, null, FIELD_BOARD_CLICK_COUNT + " DESC");

        long _id;
        while (c.moveToNext()) {
        	if (DEBUG_LOG) {
        		Log.d("[" + c.getLong(c.getColumnIndex(FIELD_BASECOLMUNS_ID)) + "]"
        				+ " name: " + c.getString(c.getColumnIndex(FIELD_BOARD_URI_HOST))
        				+ ", count: " + c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)));
        	}
        	_id = c.getLong(c.getColumnIndex(FIELD_BASECOLMUNS_ID));
        	ContentValues cv = new ContentValues();
        	cv.put(FIELD_BASECOLMUNS_ID, _id);
        	cv.put(FIELD_BOARD_CLICK_COUNT, c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)) + 1);
        	cv.put(FIELD_BOARD_TIMESTAMP, System.currentTimeMillis());
        	
            where = FIELD_BASECOLMUNS_ID + " = " + _id;
        	cr.update(CacheProvider.BOARD_URI, cv, where, null);
        	break;
        }
        
		c.close();
	}
}
