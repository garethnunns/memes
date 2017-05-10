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
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "memes.db";

    public SQLiteDatabase theDB;

    public MemesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        theDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        theDB = db;

        String sql = // create the meme table first
        "CREATE TABLE IF NOT EXISTS " +
        MemesContract.Tables.TABLE_MEME + " ( " +
        MemesContract.Tables.MEME_IDMEME +
        " INT NOT NULL, " +
        MemesContract.Tables.MEME_IDUSER +
        " INT NULL COMMENT 'The user that posted it', " +
        MemesContract.Tables.MEME_THUMB +
        " VARCHAR(150) NULL COMMENT 'Full URL to thumbnail', " +
        MemesContract.Tables.MEME_FULL +
        " VARCHAR(150) NULL COMMENT 'Full URL to large image', " +
        MemesContract.Tables.MEME_LINK +
        " VARCHAR(150) NULL COMMENT 'Full URL to the meme on the web server', " +
        MemesContract.Tables.MEME_EPOCH +
        " INT UNSIGNED NULL COMMENT 'Order by this', " +
        MemesContract.Tables.MEME_AGO +
        " VARCHAR(10) NULL, " +
        MemesContract.Tables.MEME_CAPTION +
        " VARCHAR(140) NULL, " +
        MemesContract.Tables.MEME_LAT +
        " DECIMAL(10,6) NULL, " +
        MemesContract.Tables.MEME_LONG +
        " DECIMAL(10,6) NULL, " +
        MemesContract.Tables.MEME_OPOST +
        " INT NULL COMMENT 'Id of the original post (if it’s a repost)', " +
        MemesContract.Tables.MEME_OPOSTER +
        " INT NULL COMMENT 'id of the original poster (if it’s a repost)', " +
        MemesContract.Tables.MEME_STARS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_COMMENTS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_COMMENTS +
        " TINYTEXT NULL, " +
        MemesContract.Tables.MEME_REPOSTS_NUM +
        " INT NULL, " +
        MemesContract.Tables.MEME_REPOSTED +
        " TINYINT(1) NULL COMMENT 'Whether the user has reposted this post', " +
        MemesContract.Tables.MEME_REPOSTABLE +
        " TINYINT(1) NULL COMMENT 'Whether the meme is reportable');" +
        // then the user table
        "CREATE TABLE IF NOT EXISTS " +
        MemesContract.Tables.TABLE_USER + " ( " +
        MemesContract.Tables.USER_IDUSER +
        " INT NOT NULL, " +
        MemesContract.Tables.USER_USERNAME +
        " VARCHAR(50) NULL, " +
        MemesContract.Tables.USER_FIRSTNAME +
        " VARCHAR(60) NULL, " +
        MemesContract.Tables.USER_SURNAME +
        " VARCHAR(60) NULL, " +
        MemesContract.Tables.USER_NAME +
        " VARCHAR(121) NULL COMMENT 'Length allows for firstName + surname', " +
        MemesContract.Tables.USER_PIC +
        " VARCHAR(150) NULL COMMENT 'Full URL to profile pic', " +
        MemesContract.Tables.USER_FOLLOWING +
        " TINYINT(1) NULL COMMENT 'Whether the user is following them', " +
        MemesContract.Tables.USER_YOU +
        " TINYINT(1) NULL COMMENT 'To save comparing with stored user id'" +
        ");";

        db.execSQL(sql);
        Log.i("Memestagram","MemesDBHelper onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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