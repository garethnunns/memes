package com.garethnunns.memestagram;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by gareth on 09/05/2017.
 */

public class MemesDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DB_VERSION = 15;
    public static final String DB_NAME = "memes.db";

    public SQLiteDatabase theDB;

    public MemesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        theDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        theDB = db;

        String meme = // create the meme table first
        "CREATE TABLE IF NOT EXISTS " +
        MemesContract.Tables.TABLE_MEME + " ( " +
        MemesContract.Tables.MEME_ID +
        " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        MemesContract.Tables.MEME_IDMEME +
        " INT NOT NULL, " +
        MemesContract.Tables.MEME_IDUSER +
        " INT NULL, " +
        MemesContract.Tables.MEME_THUMB +
        " VARCHAR(150) NULL, " +
        MemesContract.Tables.MEME_FULL +
        " VARCHAR(150) NULL, " +
        MemesContract.Tables.MEME_LINK +
        " VARCHAR(150) NULL, " +
        MemesContract.Tables.MEME_EPOCH +
        " INT UNSIGNED NULL, " +
        MemesContract.Tables.MEME_AGO +
        " VARCHAR(10) NULL, " +
        MemesContract.Tables.MEME_CAPTION +
        " VARCHAR(140) NULL, " +
        MemesContract.Tables.MEME_LAT +
        " DECIMAL(10,6) NULL, " +
        MemesContract.Tables.MEME_LONG +
        " DECIMAL(10,6) NULL, " +
        MemesContract.Tables.MEME_OPOST +
        " INT NULL, " +
        MemesContract.Tables.MEME_OPOSTER_ID +
        " INT NULL, " +
        MemesContract.Tables.MEME_OPOSTER_USERNAME +
        " VARCHAR(50) NULL, " +
        MemesContract.Tables.MEME_STARS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_COMMENTS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_COMMENTS +
        " TINYTEXT NULL, " +
        MemesContract.Tables.MEME_REPOSTS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_REPOSTED +
        " TINYINT(1) NULL, " +
        MemesContract.Tables.MEME_REPOSTABLE +
        " TINYINT(1) NULL " +
        ");";

        db.execSQL(meme);

        String user = " CREATE TABLE IF NOT EXISTS " +
        MemesContract.Tables.TABLE_USER + " ( " +
        MemesContract.Tables.USER_ID +
        " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        MemesContract.Tables.USER_IDUSER +
        " INT NOT NULL, " +
        MemesContract.Tables.USER_USERNAME +
        " VARCHAR(50) NULL, " +
        MemesContract.Tables.USER_FIRSTNAME +
        " VARCHAR(60) NULL, " +
        MemesContract.Tables.USER_SURNAME +
        " VARCHAR(60) NULL, " +
        MemesContract.Tables.USER_NAME +
        " VARCHAR(121) NULL, " +
        MemesContract.Tables.USER_PIC +
        " VARCHAR(150) NULL, " +
        MemesContract.Tables.USER_FOLLOWING +
        " TINYINT(1) NULL, " +
        MemesContract.Tables.USER_YOU +
        " TINYINT(1) NULL" +
        ");";
        db.execSQL(user);

        Log.i("Memestagram","MemesDBHelper onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // wipe the databases and start again
        String drop = "DROP TABLE IF EXISTS " +
                MemesContract.Tables.TABLE_MEME +
                "; DROP TABLE IF EXISTS " +
                MemesContract.Tables.TABLE_USER;
        db.execSQL(drop);
        onCreate(db);
    }

    public int clearMeme() {
        return theDB.delete(MemesContract.Tables.TABLE_MEME,null,null);
    }

    public int clearUser() {
        return theDB.delete(MemesContract.Tables.TABLE_USER,null,null);
    }
}