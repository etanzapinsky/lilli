package com.lilli.gulliver;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/11/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final String DBDIR = "lilli";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "stats.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_STATS =
            "CREATE TABLE " + StatContract.StatEntry.TABLE_NAME + " (" +
                    StatContract.StatEntry._ID + " INTEGER PRIMARY KEY," +
                    StatContract.StatEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    StatContract.StatEntry.COLUMN_NAME_SIZE + INT_TYPE + COMMA_SEP +
                    StatContract.StatEntry.COLUMN_NAME_TIME + INT_TYPE + COMMA_SEP +
                    StatContract.StatEntry.COLUMN_NAME_METHOD + TEXT_TYPE  + " )";
    private static final String SQL_DELETE_STATS =
            "DROP TABLE IF EXISTS " + StatContract.StatEntry.TABLE_NAME;


    public StatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        @Override
//        public File getDatabasePath(String name) {
//            File dbdir = new File(Environment.getExternalStoragePublicDirectory(Environment.MEDIA_SHARED), DBDIR);
//            if (!dbdir.mkdirs()) {
//                Log.e("STATS_DB_HELPER", "Directory not created");
//            }
//            File file = new File(dbdir, name);
//            Log.d("STATS_DB_HELPER", file.getAbsolutePath());
//            return file;
//        }

    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Very simple upgrade policy -> delete everything and start redownloading info
        db.execSQL(SQL_DELETE_STATS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}