package net.okjsp.provider;

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
import android.util.Log;

public class OkjspProvider extends ContentProvider implements DbConst {
	protected static final String TAG = "OkjspProvider";
	
    protected SQLiteDatabase mDb = null;
    protected DbHelper mDbHelper = null;

	// public constants for client development
    public static final String AUTHORITY = "com.kth.peloton";
	public static final Uri TRACK_URI = Uri.parse("content://" + AUTHORITY + "/" + Tracks.CONTENT_PATH);;
    public static final Uri LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/" + Locations.CONTENT_PATH);;

	// helper constants for use with the UriMatcher
    protected static final int TRACK_LIST = 1;
    protected static final int TRACK_ID = 2;
    protected static final int LOCATION_LIST = 3;
    protected static final int LOCATION_ID = 4;
    
    protected static final UriMatcher URI_MATCHER;

	/**
	* Column and content type definitions for the Provider.
	*/
	public static interface Tracks extends BaseColumns {
	    public static final String CONTENT_PATH = "tracks";
	    public static final String CONTENT_POSTFIX = "/vnd.com.kth.peloton";
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String[] PROJECTION_ALL = { _ID,
	        FIELD_TRACK_NAME, FIELD_UPDATED_AT, FIELD_CREATED_AT
	    };
	    public static final String SORT_ORDER_DEFAULT = FIELD_CREATED_AT + " ASC";
	}

    /**
    * Column and content type definitions for the Provider.
    */
    public static interface Locations extends BaseColumns {
        public static final String CONTENT_PATH = "locations";
        public static final String CONTENT_POSTFIX = "/vnd.com.kth.peloton";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_POSTFIX;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_POSTFIX;
        public static final String[] PROJECTION_ALL = { _ID,
            FIELD_TRACK_ID, FIELD_LATITUDE, FIELD_LONGITUDE, FIELD_ALTITUDE, FIELD_ACCURAY, FIELD_UPDATED_AT, FIELD_CREATED_AT
        };
        public static final String SORT_ORDER_DEFAULT = FIELD_TRACK_ID + " ASC";
    }
	
	// prepare the UriMatcher
	static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Tracks.CONTENT_PATH, TRACK_LIST);
        URI_MATCHER.addURI(AUTHORITY, Tracks.CONTENT_PATH + "/#", TRACK_ID);
        URI_MATCHER.addURI(AUTHORITY, Locations.CONTENT_PATH, LOCATION_LIST);
        URI_MATCHER.addURI(AUTHORITY, Locations.CONTENT_PATH + "/#", LOCATION_ID);
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
          case TRACK_LIST:
             return Tracks.CONTENT_TYPE;
          case TRACK_ID:
             return Tracks.CONTENT_ITEM_TYPE;
          case LOCATION_LIST:
              return Locations.CONTENT_TYPE;
          case LOCATION_ID:
              return Locations.CONTENT_ITEM_TYPE;
          default:
             throw new IllegalArgumentException("Unsupported URI: " + uri);
       }
	}

    public String getTableName(Uri uri) {
       switch (URI_MATCHER.match(uri)) {
          case TRACK_LIST:
             return TABLE_TRACK;
          case TRACK_ID:
             return TABLE_TRACK;
          case LOCATION_LIST:
              return TABLE_LOCATION;
          case LOCATION_ID:
              return TABLE_LOCATION;
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
	    
        if (URI_MATCHER.match(uri) != TRACK_LIST && URI_MATCHER.match(uri) != LOCATION_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        
        long id = mDb.insert(getTableName(uri), null, values);
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
            case TRACK_LIST:
            case LOCATION_LIST:
                delCount = mDb.delete(getTableName(uri), selection, selectionArgs);
                break;
                
            case TRACK_ID:
            case LOCATION_ID:
                String idStr = uri.getLastPathSegment();
                String where;
                if (URI_MATCHER.match(uri) == TRACK_ID) {
                    where = FIELD_TRACK_ID + " = " + idStr;
                } else {
                    where = FIELD_LOCATION_ID + " = " + idStr;
                }
                if (!TextUtils.isEmpty(selection)) {
                   where += " AND " + selection;
                }
                delCount = mDb.delete(getTableName(uri), where, selectionArgs);
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
            case TRACK_LIST:
            case LOCATION_LIST:
                // all nice and well
                break;
            case TRACK_ID:
            case LOCATION_ID:
                // limit query to one row at most:
                builder.appendWhere(BaseColumns._ID + " = " + uri.getLastPathSegment());
                
                if (TextUtils.isEmpty(sortOrder)) { 
                    if (URI_MATCHER.match(uri) == TRACK_ID) {
                        sortOrder = FIELD_CREATED_AT;
                    } else {
                        sortOrder = FIELD_TRACK_ID;
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
            case TRACK_LIST:
            case LOCATION_LIST:
                updateCount = mDb.update(getTableName(uri), values, selection, selectionArgs); 
                break;
            case TRACK_ID:
            case LOCATION_ID:
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
