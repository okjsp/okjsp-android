package net.okjsp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.okjsp.Const;
import net.okjsp.R;
import net.okjsp.provider.DbConst;
import net.okjsp.provider.OkjspProvider;
import net.okjsp.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;

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
			loadBoardList();
		}
		
		if (DEBUG_LOG) {
			int count = 0;
			for(Board board : mBoardList) {
				Log.d("[" + count++ + "]:" + board.getTitle());
			}
		}
	}
	
	public ArrayList<Board> getBoardList() {
		return mBoardList;
	}
	
	public void initAnchorList() {
        final Resources res = mContext.getResources();
        String titles[] = res.getStringArray(R.array.anchor_names);
        String urls[] = res.getStringArray(R.array.anchor_links);

        for(int i = 0; i < titles.length; i++) {
        	Board board = new Board(titles[i], urls[i]);
        	mBoardList.add(board);
        }
	}
	
	public void initBoardList() {
        ContentResolver cr = mContext.getContentResolver();
        final Resources res = mContext.getResources();
        String titles[] = res.getStringArray(R.array.actions_names);
        String urls[] = res.getStringArray(R.array.actions_links);
        
        initAnchorList();
        
        for(int i = 0; i < titles.length; i++) {
        	ContentValues cv = new ContentValues();
        	Uri uri = Uri.parse(urls[i]);
        	cv.put(FIELD_BOARD_NAME, uri.getHost());
        	cv.put(FIELD_BOARD_DISPLAY_NAME, titles[i]);
        	if ("notice".equals(uri.getHost())) {
	        	cv.put(FIELD_BOARD_CLICK_COUNT, Integer.MAX_VALUE);
        	} else if ("recent".equals(uri.getHost())) {
	        	cv.put(FIELD_BOARD_CLICK_COUNT, Integer.MAX_VALUE - 1);
        	}  
        	cv.put(FIELD_CREATED_AT, System.currentTimeMillis());
        	
        	cr.insert(OkjspProvider.BOARD_URI, cv);
        	
        	Board board = new Board(titles[i], urls[i]);
        	mBoardList.add(board);
        }
	}
	
	public int loadBoardList() {
		int count = 0;
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(OkjspProvider.BOARD_URI, OkjspProvider.Board.PROJECTION_ALL, null, null, 
        		FIELD_BOARD_CLICK_COUNT + " DESC"
        		+ ", " + FIELD_BOARD_TIMESTAMP + " DESC"
        		+ ", " + FIELD_BOARD_NAME + " ASC");

        count = c.getCount();
        if (DEBUG_LOG) {
        	Log.d("board count in DB: " + c.getCount());
        }
        
        if (count > 0) {
        	initAnchorList();
        }
        
        while(c.moveToNext()) {
        	if (DEBUG_LOG) {
        		Log.d("name:" + c.getString(c.getColumnIndex(FIELD_BOARD_NAME))
        				+ ", clicked:" + c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)));
        	}
        }
        
        c.close();
        
        return count;
	}
	
	public void onBoadClicked(String board) {
		if ("notice".equals(board) || "recent".equals(board)) return;
		
        ContentResolver cr = mContext.getContentResolver();
        String where = FIELD_BOARD_NAME + " = '" + board + "'";
        Cursor c = cr.query(OkjspProvider.BOARD_URI, OkjspProvider.Board.PROJECTION_ALL, 
        		where, null, FIELD_BOARD_CLICK_COUNT + " DESC");

        long _id;
        while (c.moveToNext()) {
        	if (DEBUG_LOG) {
        		Log.d("[" + c.getLong(c.getColumnIndex(FIELD_BASECOLMUNS_ID)) + "]"
        				+ " name: " + c.getString(c.getColumnIndex(FIELD_BOARD_NAME))
        				+ ", count: " + c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)));
        	}
        	_id = c.getLong(c.getColumnIndex(FIELD_BASECOLMUNS_ID));
        	ContentValues cv = new ContentValues();
        	cv.put(FIELD_BASECOLMUNS_ID, _id);
        	cv.put(FIELD_BOARD_CLICK_COUNT, c.getInt(c.getColumnIndex(FIELD_BOARD_CLICK_COUNT)) + 1);
        	cv.put(FIELD_BOARD_TIMESTAMP, System.currentTimeMillis());
        	
            where = FIELD_BASECOLMUNS_ID + " = " + _id;
        	cr.update(OkjspProvider.BOARD_URI, cv, where, null);
        	break;
        }
        
		c.close();
	}
	
	private final Comparator<Board> comparator = new Comparator<Board>() {
		@Override
		public int compare(Board obj1, Board obj2) {
			int result = obj1.getClickCount() - obj2.getClickCount();
			if (result == 0) {
				result = (int)(obj1.getTimeStamp() - obj2.getTimeStamp());
			}
			return (result == 0) ? (obj1.getTitle().compareTo(obj2.getTitle())) : result;
		}
	};	
	
}
