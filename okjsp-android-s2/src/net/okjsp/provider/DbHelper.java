package net.okjsp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper implements DbConst {
	protected static final String TAG = "DbHelper";
    protected static final String _ID = "_id";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getTableTrackSql_v3());
        db.execSQL(getTableLocationSql_v3());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade:" + db.getVersion() + ", " + oldVersion + ", " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(db); 
    }

    protected String getTableTrackSql_v3() {
        StringBuilder table_track = new StringBuilder();
        table_track.append("CREATE TABLE ").append(TABLE_TRACK).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_SERIAL_NO).append(" INTEGER, ")
                .append(FIELD_TRACK_NAME).append(" TEXT, ")
                .append(FIELD_TRACK_ADDRESS).append(" TEXT, ")
                .append(FIELD_DATA_SIZE).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_track.toString();
    }
    
    protected String getTableLocationSql_v3() {
        StringBuilder table_location = new StringBuilder();
        table_location.append("CREATE TABLE ").append(TABLE_LOCATION).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_SERIAL_NO).append(" INTEGER, ")
                .append(FIELD_TRACK_ID).append(" INTEGER NOT NULL, ")
                .append(FIELD_LATITUDE).append(" REAL, ")
                .append(FIELD_LONGITUDE).append(" REAL, ")
                .append(FIELD_ALTITUDE).append(" REAL, ")
                .append(FIELD_ACCURAY).append(" REAL, ")
                .append(FIELD_BEARING).append(" REAL, ")
                .append(FIELD_SPEED).append(" REAL, ")
                .append(FIELD_TIME).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_location.toString();
    }
    
    protected String getTableTrackSql_v2() {
        StringBuilder table_track = new StringBuilder();
        table_track.append("CREATE TABLE ").append(TABLE_TRACK).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_TRACK_NAME).append(" TEXT, ")
                .append(FIELD_TRACK_ADDRESS).append(" TEXT, ")
                .append(FIELD_DATA_SIZE).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_track.toString();
    }
    
    protected String getTableLocationSql_v2() {
        StringBuilder table_location = new StringBuilder();
        table_location.append("CREATE TABLE ").append(TABLE_LOCATION).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_TRACK_ID).append(" INTEGER NOT NULL, ")
                .append(FIELD_LATITUDE).append(" REAL, ")
                .append(FIELD_LONGITUDE).append(" REAL, ")
                .append(FIELD_ALTITUDE).append(" REAL, ")
                .append(FIELD_ACCURAY).append(" REAL, ")
                .append(FIELD_BEARING).append(" REAL, ")
                .append(FIELD_SPEED).append(" REAL, ")
                .append(FIELD_TIME).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_location.toString();
    }
    
    protected String getTableTrackSql_v1() {
        StringBuilder table_track = new StringBuilder();
        table_track.append("CREATE TABLE ").append(TABLE_TRACK).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_TRACK_NAME).append(" TEXT, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_track.toString();
    }
    
    protected String getTableLocationSql_v1() {
        StringBuilder table_location = new StringBuilder();
        table_location.append("CREATE TABLE ").append(TABLE_LOCATION).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_TRACK_ID).append(" INTEGER NOT NULL, ")
                .append(FIELD_LATITUDE).append(" REAL, ")
                .append(FIELD_LONGITUDE).append(" REAL, ")
                .append(FIELD_ALTITUDE).append(" REAL, ")
                .append(FIELD_ACCURAY).append(" REAL, ")
                .append(FIELD_BEARING).append(" REAL, ")
                .append(FIELD_SPEED).append(" REAL, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_location.toString();
    }
}
