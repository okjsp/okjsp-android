package net.okjsp.provider;

import android.provider.BaseColumns;

public interface DbConst {
    public static final String DATABASE_NAME = "track.db";
    public static final int DATABASE_VERSION = 3;
    
    public static final String TABLE_TRACK = "t_track";
    public static final String TABLE_LOCATION = "t_location";
    
    /* COMMON FIELD */
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_UPDATED_AT = "updated_at";
    public static final String FIELD_BASECOLMUNS_ID = BaseColumns._ID;
    public static final String FIELD_SERIAL_NO = "serial_no";
    
    /* TRACK */
    public static final String FIELD_TRACK_ID = "track_id";
    public static final String FIELD_TRACK_NAME = "track_name";
    public static final String FIELD_TRACK_ADDRESS = "track_address";
    public static final String FIELD_DATA_SIZE = "data_size";

    /* LOCATION DATA */
    public static final String FIELD_LOCATION_ID    = "location_id";
    public static final String FIELD_LATITUDE       = "latitude";
    public static final String FIELD_LONGITUDE      = "longitude";
    public static final String FIELD_ALTITUDE       = "altitude";
    public static final String FIELD_ACCURAY        = "accuracy";
    public static final String FIELD_BEARING        = "bearing";
    public static final String FIELD_TIME           = "time";
    public static final String FIELD_SPEED          = "speed";
}
