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
    public static final int USERS = 101;
    public static final int MEME = 200;
    public static final int USER = 201;
    private static final UriMatcher theUriMatcher = buildUriMatcher();
    public static MemesDBHelper theDBHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES,FEED);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES+"/#",MEME);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_USERS,USERS);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_USERS+"/#",USER);

        return matcher;
    }

    public MemesContentProvider() {
    }

    @Override
    public boolean onCreate() {
        theDBHelper = new MemesDBHelper(getContext());
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
            case USERS:
                return MemesContract.Tables.USERS_CONTENT_TYPE_DIR;
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

        // TODO: update instead of delete when it already exists

        switch(theUriMatcher.match(uri)) {
            case FEED:{ // same as an individual meme (below)
            }
            case MEME:{
                try {
                    Long iduser = values.getAsLong(MemesContract.Tables.MEME_IDMEME);
                    delete(MemesContract.Tables.buildMemeUriWithID(iduser), null, null);
                }
                catch (NullPointerException e) {
                    delete(MemesContract.Tables.MEMES_CONTENT_URI,null,null);
                }

                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                long _id = db.insert(MemesContract.Tables.TABLE_MEME,null,values);
                if (_id > 0)
                    retUri = MemesContract.Tables.buildMemeUriWithID(values.getAsLong(MemesContract.Tables.MEME_IDMEME));
                else
                    throw new SQLException("Failed to insert meme");
                break;
            }
            case USERS: { // same as an individual meme (below)
            }
            case USER:{
                try {
                    Long iduser = values.getAsLong(MemesContract.Tables.USER_IDUSER);
                    delete(MemesContract.Tables.buildUserUriWithID(iduser), null, null);
                }
                catch (NullPointerException e) {
                    delete(MemesContract.Tables.USERS_CONTENT_URI,null,null);
                }

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
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                return db.update(MemesContract.Tables.TABLE_MEME,values,selection,selectionArgs);
            }
            case MEME:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                Long id = ContentUris.parseId(uri);
                Log.i(LOG_TAG, "Updating meme "+id);
                String[] args = new String[] {String.valueOf(id)};
                return db.update(MemesContract.Tables.TABLE_MEME,values,MemesContract.Tables.MEME_IDMEME+" = ?",args);
            }
            case USERS: {
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                return db.update(MemesContract.Tables.TABLE_USER,values,selection,selectionArgs);
            }
            case USER:{
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                Long id = ContentUris.parseId(uri);
                Log.i(LOG_TAG, "Updating meme "+id);
                String[] args = new String[] {String.valueOf(id)};
                return db.update(MemesContract.Tables.TABLE_USER,values,MemesContract.Tables.USER_IDUSER+" = ?",args);
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
            case USERS: {
                Log.i(LOG_TAG, "delete() all users");
                return theDBHelper.clearUser();
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
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_MEME+
                        ", " + MemesContract.Tables.TABLE_USER +
                        " WHERE " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " AND " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_FEED + " = 1 " +
                        " ORDER BY " + MemesContract.Tables.MEME_EPOCH + " DESC";

                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,null);

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
