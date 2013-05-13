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
        db.execSQL(getTableBoardSql_v1());
        db.execSQL(getTablePostSql_v2());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade:" + db.getVersion() + ", " + oldVersion + ", " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOARD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POST);
        onCreate(db); 
    }

    protected String getTableBoardSql_v1() {
        StringBuilder table_post = new StringBuilder();
        table_post.append("CREATE TABLE ").append(TABLE_BOARD).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_BOARD_URI_HOST).append(" TEXT NOT NULL, ")
                .append(FIELD_BOARD_DISPLAY_NAME).append(" TEXT NOT NULL, ")
                .append(FIELD_BOARD_CLICK_COUNT).append(" INTEGER, ")
                .append(FIELD_BOARD_TIMESTAMP).append(" INTEGER, ")
                .append(FIELD_BOARD_MONITORING).append(" INTEGER, ")
                .append(FIELD_BOARD_INDEX).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_post.toString();
    }
    
    protected String getTablePostSql_v2() {
        StringBuilder table_post = new StringBuilder();
        table_post.append("CREATE TABLE ").append(TABLE_POST).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_POST_ID).append(" INTEGER NOT NULL, ")
                .append(FIELD_POST_URL).append(" TEXT, ")
                .append(FIELD_POST_TITLE).append(" TEXT, ")
                .append(FIELD_BOARD_URI_HOST).append(" TEXT, ")
                .append(FIELD_BOARD_DISPLAY_NAME).append(" TEXT, ")
                .append(FIELD_POST_WRITER_NAME).append(" TEXT, ")
                .append(FIELD_POST_WRITER_PHOTO_URL).append(" TEXT, ")
                .append(FIELD_POST_CLICK_COUNT).append(" INTEGER, ")
                .append(FIELD_POST_CONTENT).append(" TEXT, ")
                .append(FIELD_POST_TIMESTAMP).append(" TEXT, ")
                .append(FIELD_POST_ISREAD).append(" INTEGER, ")
                .append(FIELD_POST_COMMENT_COUNT).append(" INTEGER, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return table_post.toString();
    }
}
