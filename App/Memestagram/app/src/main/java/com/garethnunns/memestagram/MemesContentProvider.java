package com.garethnunns.memestagram;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by gareth on 09/05/2017.
 * content provider for the app
 */

public class MemesContentProvider extends ContentProvider {
    public static final String LOG_TAG = "MemesContentProvider";
    public static final int MEMES = 100;
    public static final int USERS = 101;
    public static final int FEEDS = 102;
    public static final int HOTS = 103;
    public static final int STARREDS = 104;
    public static final int PROFILES = USERS; // same as users
    public static final int MEME = 200;
    public static final int USER = 201;
    public static final int FEED = 202;
    public static final int HOT = 203;
    public static final int STARRED = 204;
    public static final int PROFILE = 205;
    private static final UriMatcher theUriMatcher = buildUriMatcher();
    public static MemesDBHelper theDBHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES,MEMES);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_FEED,FEEDS);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_HOT,HOTS);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_STARRED,STARREDS);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_PROFILE,PROFILES);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_MEMES+"/#",MEME);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_USERS,USERS);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_USERS+"/#",USER);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_FEED+"/#",FEED);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_HOT+"/#",HOT);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_STARRED+"/#",STARRED);

        matcher.addURI(MemesContract.CONTENT_AUTHORITY,MemesContract.PATH_PROFILE+"/#",PROFILE);

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
            case MEMES:
                return MemesContract.Tables.MEMES_CONTENT_TYPE_DIR;
            case FEEDS:
                return MemesContract.Tables.FEED_CONTENT_TYPE_DIR;
            case HOTS:
                return MemesContract.Tables.HOT_CONTENT_TYPE_DIR;
            case STARREDS:
                return MemesContract.Tables.STARRED_CONTENT_TYPE_DIR;
            case MEME:
                return MemesContract.Tables.MEMES_CONTENT_TYPE_ITEM;
            case USERS:
                return MemesContract.Tables.USERS_CONTENT_TYPE_DIR;
            case USER:
                return MemesContract.Tables.USERS_CONTENT_TYPE_ITEM;
            case FEED:
                return MemesContract.Tables.FEED_CONTENT_TYPE_ITEM;
            case HOT:
                return MemesContract.Tables.HOT_CONTENT_TYPE_ITEM;
            case STARRED:
                return MemesContract.Tables.STARRED_CONTENT_TYPE_ITEM;
            case PROFILE:
                return MemesContract.Tables.PROFILE_CONTENT_TYPE_ITEM;
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
            case MEMES: // same as an individual meme (below)
            case FEEDS:
            case HOTS:
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
            case USERS: // same as an individual meme (below)
            case HOT:
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
            case FEED: {
                Long id = ContentUris.parseId(uri);
                Log.i(LOG_TAG, "Inserting into feed "+id);
                String[] args = new String[] {String.valueOf(id)};
                ContentValues add = new ContentValues();
                add.put(MemesContract.Tables.MEME_FEED, 1);
                update(uri,add,MemesContract.Tables.MEME_IDMEME+" = ?",args);
                break;
            }
            default:
                throw new UnsupportedOperationException("Insert not yet implemented for "+uri.toString());
        }

        Log.i(LOG_TAG, "Insert success");
        return retUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(LOG_TAG, "update()");

        switch(theUriMatcher.match(uri)) {
            case FEEDS:
            case HOTS:
            case MEMES: {
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                return db.update(MemesContract.Tables.TABLE_MEME,values,selection,selectionArgs);
            }
            case FEED:
            case HOT:
            case MEME: {
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
            case USER: {
                SQLiteDatabase db = theDBHelper.getWritableDatabase();
                Long id = ContentUris.parseId(uri);
                Log.i(LOG_TAG, "Updating meme "+id);
                String[] args = new String[] {String.valueOf(id)};
                return db.update(MemesContract.Tables.TABLE_USER,values,MemesContract.Tables.USER_IDUSER+" = ?",args);
            }
            default:
                throw new UnsupportedOperationException("Update not yet implemented for "+uri.toString());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(theUriMatcher.match(uri)) {
            case MEMES: {
                Log.i(LOG_TAG, "delete() all memes");
                return theDBHelper.clearMeme();
            }
            case FEEDS:
            case HOTS: {
                ContentValues clear = new ContentValues();
                switch(theUriMatcher.match(uri)) {
                    case(FEEDS):
                        clear.put(MemesContract.Tables.MEME_FEED, 0);
                        break;
                    case(HOTS):
                        clear.put(MemesContract.Tables.MEME_HOT, 0);
                        break;
                }
                return update(MemesContract.Tables.MEMES_CONTENT_URI,clear,null,null);
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
            case FEED:
            case HOT: {
                Long id = ContentUris.parseId(uri);
                Log.i(LOG_TAG, "Deleting from feed "+id);
                String[] args = new String[] {String.valueOf(id)};
                ContentValues clear = new ContentValues();
                switch(theUriMatcher.match(uri)) {
                    case(FEED):
                        clear.put(MemesContract.Tables.MEME_FEED, 0);
                        break;
                    case(HOT):
                        clear.put(MemesContract.Tables.MEME_HOT, 0);
                        break;
                }
                return update(uri,clear,MemesContract.Tables.MEME_IDMEME+" = ?",args);
            }
            default:
                throw new UnsupportedOperationException("Delete not yet implemented for "+uri.toString());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch(theUriMatcher.match(uri)) {
            case FEEDS: {
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_MEME+
                        ", " + MemesContract.Tables.TABLE_USER +
                        " WHERE " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " AND " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_FEED + " = 1 " +
                        " ORDER BY " + MemesContract.Tables.MEME_EPOCH + " DESC";

                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,null);

                Log.i(LOG_TAG, "Returning all "+retCursor.getCount()+" memes for the meme feed");
                break;
            }
            case HOTS: {
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_MEME+
                        ", " + MemesContract.Tables.TABLE_USER +
                        " WHERE " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " AND " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_HOT + " > 0 " +
                        " ORDER BY " + MemesContract.Tables.MEME_HOT + " ASC";

                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,null);

                Log.i(LOG_TAG, "Returning all "+retCursor.getCount()+" memes for the hot feed");
                break;
            }
            case STARREDS: {
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_MEME+
                        ", " + MemesContract.Tables.TABLE_USER +
                        " WHERE " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " AND " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_STARRED + " = 1 " +
                        " ORDER BY " + MemesContract.Tables.MEME_EPOCH + " DESC";

                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,null);

                Log.i(LOG_TAG, "Returning all "+retCursor.getCount()+" memes for the starred feed");
                break;
            }
            case MEME: {
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_MEME+
                        ", " + MemesContract.Tables.TABLE_USER +
                        " WHERE " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " AND " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDMEME + " = ? " +
                        " ORDER BY " + MemesContract.Tables.MEME_EPOCH + " DESC LIMIT 1";

                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,args);

                Log.i(LOG_TAG, "Returning the meme "+ContentUris.parseId(uri));
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
            case PROFILE: {
                String sql = "SELECT "+MemesContract.Tables.TABLE_MEME+".*, "+
                        MemesContract.Tables.TABLE_USER+".* " +
                        "FROM " + MemesContract.Tables.TABLE_USER +
                        " LEFT JOIN " + MemesContract.Tables.TABLE_MEME +
                        " ON " + MemesContract.Tables.TABLE_MEME + "." + MemesContract.Tables.MEME_IDUSER +
                        " = " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER +
                        " WHERE " + MemesContract.Tables.TABLE_USER + "." + MemesContract.Tables.USER_IDUSER + " = ? " +
                        " ORDER BY " + MemesContract.Tables.MEME_EPOCH + " DESC";

                String[] args = new String[] {Long.toString(ContentUris.parseId(uri))};
                retCursor = theDBHelper.getReadableDatabase().rawQuery(sql,args);

                Log.i(LOG_TAG, "Returning all "+retCursor.getCount()+" memes for the profile feed");
                break;
            }
            default:
                throw new UnsupportedOperationException("Query not yet implemented for "+uri.toString());
        }

        return retCursor;
    }
}
