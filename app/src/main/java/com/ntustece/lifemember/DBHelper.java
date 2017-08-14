package com.ntustece.lifemember;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;

/**
 * Created by petingo on 2017/8/14.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="db";
    public static final int VERSION=1;
    public DBHelper(Context context){
        super(context, DATABASE_NAME, null,VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `data` (" +
                "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
                "`time` TEXT," +
                "`item` TEXT," +
                "`price` INTEGER," +
                "`store` TEXT," +
                "`ticketID` TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
