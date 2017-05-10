package com.garethnunns.memestagram;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by gareth on 09/05/2017.
 */

public class MemesContentProvider extends ContentProvider {
    public static final String LOG_TAG = "MemesContentProvider";
    public static final int FEED = 100;
    public static final int MEME = 201;
    public static final int USER = 202;
    private static final UriMatcher theUriMatcher = buildUriMatcher();
    public static MemesDBHelper theDBHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES,FEED);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES+"/#",MEME);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_USERS+"/#",USER);

        return matcher;
    }

    public MemesContentProvider() {
    }

    @Override
    public boolean onCreate() {
        theDBHelper = new MemesDBHelper(getContext(),MemesDBHelper.DB_NAME,null,MemesDBHelper.DB_VERSION);
        Log.i(LOG_TAG, "onCreate()");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch(theUriMatcher.match(uri)) {
            case FEED:
                return MemesContract.Tables.MEMES_CONTENT_TYPE_DIR;
            case MEME:
                return MemesContract.Tables.MEMES_CONTENT_TYPE_ITEM;
            case USER:
                return MemesContract.Tables.USERS_CONTENT_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(LOG_TAG, "insert()");
        Uri retUri = null;

        switch(theUriMatcher.match(uri)) {
            case FEED:{
            }
            case MEME:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                long _id = db.insert(MemesContract.Tables.TABLE_MEME,null,values);
                if (_id > 0)
                    retUri = MemesContract.Tables.buildMemeUriWithID(_id);
                else
                    throw new SQLException("Failed to insert meme");
                break;
            }
            case USER:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                long _id = db.insert(MemesContract.Tables.TABLE_USER,null,values);
                if (_id > 0)
                    retUri = MemesContract.Tables.buildUserUriWithID(_id);
                else
                    throw new SQLException("Failed to insert user");
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        Log.i(LOG_TAG, "Insert success");
        return retUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "update()");

        switch(theUriMatcher.match(uri)) {
            case FEED:{
            }
            case MEME:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                return db.update(MemesContract.Tables.TABLE_MEME,values,MemesContract.Tables.MEME_IDMEME+"=?",args);
            }
            case USER:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                return db.update(MemesContract.Tables.TABLE_USER,values,MemesContract.Tables.USER_IDUSER+"=?",args);
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(theUriMatcher.match(uri)) {
            case FEED: {
                Log.i(LOG_TAG, "delete() all memes");
                return theDBHelper.clearMeme();
            }
            case MEME: {
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                return db.delete(MemesContract.Tables.TABLE_MEME,MemesContract.Tables.MEME_IDMEME+"=?", args);
            }
            case USER: {
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                return db.delete(MemesContract.Tables.TABLE_USER,MemesContract.Tables.USER_IDUSER+"=?",args);
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        Cursor retCursor;

        switch(theUriMatcher.match(uri)) {
            case FEED: {
                if (TextUtils.isEmpty(sortOrder)) sortOrder = MemesContract.Tables.MEME_EPOCH + " DESC";

                retCursor = theDBHelper.getReadableDatabase().query(
                        MemesContract.Tables.TABLE_MEME, // Table to Query
                        projection, //Columns
                        null, // Columns for the "where" clause
                        null, // Values for the "where" clause
                        null, // columns to group by
                        null, // columns to filter by row groups
                        sortOrder // sort order
                );
                Log.i(LOG_TAG, "Returning all memes for the feed");
                Log.i(LOG_TAG, "Got " + retCursor.getCount() + " rows");
                break;
            }
            case MEME: {
                retCursor = theDBHelper.getReadableDatabase().query(
                        MemesContract.Tables.TABLE_MEME,
                        projection,
                        MemesContract.Tables.MEME_IDMEME + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case USER: {
                retCursor = theDBHelper.getReadableDatabase().query(
                        MemesContract.Tables.TABLE_USER,
                        projection,
                        MemesContract.Tables.USER_IDUSER + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        return retCursor;
    }
}
