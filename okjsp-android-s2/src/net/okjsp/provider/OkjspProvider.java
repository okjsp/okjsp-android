package net.okjsp.provider;

import net.okjsp.util.Log;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class OkjspProvider extends ContentProvider implements DbConst {
	protected static final String TAG = "OkjspProvider";
	protected static final boolean DEBUG_LOG = true;
	
    protected SQLiteDatabase mDb = null;
    protected DbHelper mDbHelper = null;

	// public constants for client development
    public static final String AUTHORITY = "net.okjsp.provider";
	public static final Uri BOARD_URI = Uri.parse("content://" + AUTHORITY + "/" + TableBoard.CONTENT_PATH);;
	public static final Uri POST_URI = Uri.parse("content://" + AUTHORITY + "/" + TablePost.CONTENT_PATH);;

	// helper constants for use with the UriMatcher
    protected static final int BOARD_LIST = 1;
    protected static final int BOARD_ID = 2;
    protected static final int POST_LIST = 3;
    protected static final int POST_ID = 4;
    
    protected static final UriMatcher URI_MATCHER;

	/**
	* Column and content type definitions for the Provider.
	*/
	public static interface TableBoard extends BaseColumns {
	    public static final String CONTENT_PATH = "board";
	    public static final String CONTENT_POSTFIX = "/vnd.net.okjsp.provider";
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String[] PROJECTION_ALL = { _ID,
	    	FIELD_BOARD_NAME, FIELD_BOARD_DISPLAY_NAME, FIELD_BOARD_CLICK_COUNT, FIELD_BOARD_TIMESTAMP, 
	    	FIELD_UPDATED_AT, FIELD_CREATED_AT
	    };
	    public static final String SORT_ORDER_DEFAULT = FIELD_CREATED_AT + " ASC";
	}

	public static interface TablePost extends BaseColumns {
	    public static final String CONTENT_PATH = "post";
	    public static final String CONTENT_POSTFIX = "/vnd.net.okjsp.provider";
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String[] PROJECTION_ALL = { _ID,
	    	FIELD_POST_ID, FIELD_BOARD_NAME,
	    	FIELD_POST_WRITER_NAME, FIELD_POST_WRITER_PHOTO_URL, FIELD_POST_READ_COUNT, FIELD_POST_CONTENT, 
	    	FIELD_UPDATED_AT, FIELD_CREATED_AT
	    };
	    public static final String SORT_ORDER_DEFAULT = FIELD_CREATED_AT + " ASC";
	}

	// prepare the UriMatcher
	static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, TableBoard.CONTENT_PATH, BOARD_LIST);
        URI_MATCHER.addURI(AUTHORITY, TableBoard.CONTENT_PATH + "/#", BOARD_ID);
        URI_MATCHER.addURI(AUTHORITY, TablePost.CONTENT_PATH, POST_LIST);
        URI_MATCHER.addURI(AUTHORITY, TablePost.CONTENT_PATH + "/#", POST_ID);
	}
	
	@Override
	public boolean onCreate() {
	    mDbHelper = new DbHelper(getContext());
	    mDb = mDbHelper.getWritableDatabase();
	    
        if (mDb == null) {
            return false;
        }
        
        if (mDb.isReadOnly()) {
            mDb.close();
            mDb = null;

            return false;
        }
	      
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
       switch (URI_MATCHER.match(uri)) {
          case BOARD_LIST:
             return TableBoard.CONTENT_TYPE;
          case BOARD_ID:
             return TableBoard.CONTENT_ITEM_TYPE;
          case POST_LIST:
              return TablePost.CONTENT_TYPE;
           case POST_ID:
              return TablePost.CONTENT_ITEM_TYPE;
          default:
             throw new IllegalArgumentException("Unsupported URI: " + uri);
       }
	}

    public String getTableName(Uri uri) {
       switch (URI_MATCHER.match(uri)) {
	       case BOARD_LIST:
	       case BOARD_ID:
	          return TABLE_BOARD;
          case POST_LIST:
          case POST_ID:
             return TABLE_POST;
          default:
             throw new IllegalArgumentException("Unsupported URI: " + uri);
       }
    }
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    if (mDb == null || mDb.isReadOnly()) {
	        Log.e(TAG, "Database is NULL or READ-ONLY!!!");
	        return null;
	    }
	    
        if (URI_MATCHER.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        
        long id = mDb.insert(getTableName(uri), null, values);
        
        if (DEBUG_LOG) {
        	Log.d("insert(" + uri.toString() + "): " + id);
        }
        
        if (id > 0) {
            // notify all listeners of changes and return itemUri:
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
	    
        // s.th. went wrong: 
	    throw new SQLException("Problem while inserting into " + getTableName(uri) + ", uri: " + uri); // use another exception here!!!
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mDb == null || mDb.isReadOnly()) {
            Log.e(TAG, "Database is NULL or READ-ONLY!!!");
            return 0;
        }
	    
	    int delCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case POST_LIST:
            case BOARD_LIST:
                delCount = mDb.delete(getTableName(uri), selection, selectionArgs);
                break;
                
            case POST_ID:
            case BOARD_ID:
                String idStr = uri.getLastPathSegment();
                String where = null;
                if (getType(uri).equals(POST_ID)) {
                    where = POST_ID + " = " + idStr;
                    if (!TextUtils.isEmpty(selection)) {
                       where += " AND " + selection;
                    }
                } else if (getType(uri).equals(BOARD_ID)) {
                    where = BOARD_ID + " = " + idStr;
                    if (!TextUtils.isEmpty(selection)) {
                       where += " AND " + selection;
                    }
                }
                
                if (where != null) {
                	delCount = mDb.delete(getTableName(uri), where, selectionArgs);
                } else {
                	Log.e(TAG, "'where' is NULL!!!");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        // notify all listeners of changes:
        if (delCount > 0) {
           getContext().getContentResolver().notifyChange(uri, null);
        }        
        
        return delCount;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (mDb == null) {
            Log.e(TAG, "Database is NULL!!!");
            return null;
        }
        
	   SQLiteQueryBuilder builder = new SQLiteQueryBuilder(); 
	   builder.setTables(getTableName(uri));
	   
	   switch (URI_MATCHER.match(uri)) {
            case POST_LIST:
            case BOARD_LIST:
                // all nice and well
                break;
            case POST_ID:
            case BOARD_ID:
                // limit query to one row at most:
                builder.appendWhere(BaseColumns._ID + " = " + uri.getLastPathSegment());
                
                if (TextUtils.isEmpty(sortOrder)) { 
                    if (URI_MATCHER.match(uri) == POST_ID) {
                        sortOrder = FIELD_CREATED_AT;
                    } else {
                        sortOrder = FIELD_POST_ID;
                    }
                } 
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        Cursor cursor = builder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder); 
        // if we want to be notified of any changes: 
        cursor.setNotificationUri(getContext().getContentResolver(), uri); 

        return cursor; 
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mDb == null || mDb.isReadOnly()) {
            Log.e(TAG, "Database is NULL or READ-ONLY!!!");
            return 0;
        }
	    
	    int updateCount = 0;
	    
        switch (URI_MATCHER.match(uri)) {
            case POST_LIST:
            case BOARD_LIST:
                updateCount = mDb.update(getTableName(uri), values, selection, selectionArgs); 
                break;
            case POST_ID:
            case BOARD_ID:
                String idStr = uri.getLastPathSegment(); 
                String where = BaseColumns._ID + " = " + idStr; 
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mDb.update(getTableName(uri), values, where, selectionArgs); 
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        // notify all listeners of changes:
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return updateCount;
	}
}
