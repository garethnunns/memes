package com.garethnunns.memestagram;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gareth on 09/05/2017.
 */

public class MemesContract {
    //Uri for ContentProvider
    public static final String CONTENT_AUTHORITY = "com.garethnunns.memestagram";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MEMES = "memes";
    public static final String PATH_USERS = "users";
    public static final String PATH_FEED = "feed";
    public static final String PATH_HOT = "hot";
    public static final String PATH_STARRED = "starred";

    // TODO: create feed, hot, starred, profile tables

    /* Inner class that defines the meme table contents */
    public static class Tables implements BaseColumns {
        // memes URI
        public static final Uri MEMES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMES).build();
        public static final String MEMES_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;
        public static final String MEMES_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;

        // feed URI
        public static final Uri FEED_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEED).build();
        public static final String FEED_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_FEED;
        public static final String FEED_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_FEED;

        // hot URI
        public static final Uri HOT_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOT).build();
        public static final String HOT_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_HOT;
        public static final String HOT_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_HOT;

        // starred URI
        public static final Uri STARRED_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();
        public static final String STARRED_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_STARRED;
        public static final String STARRED_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_STARRED;

        // meme table
        public static final String TABLE_MEME = "meme";
        public static final String PRE_MEME = TABLE_MEME + "_";
        public static final String MEME_ID = "_id"; // without the ID lots of things break
        public static final String MEME_IDMEME = PRE_MEME + "idmeme";
        public static final String MEME_IDUSER = PRE_MEME + "iduser";
        public static final String MEME_THUMB = PRE_MEME + "thumb";
        public static final String MEME_FULL = PRE_MEME + "full";
        public static final String MEME_LINK = PRE_MEME + "link";
        public static final String MEME_EPOCH = PRE_MEME + "epoch";
        public static final String MEME_AGO = PRE_MEME + "ago";
        public static final String MEME_CAPTION = PRE_MEME + "caption";
        public static final String MEME_LAT = PRE_MEME + "lat";
        public static final String MEME_LONG = PRE_MEME + "long";
        public static final String MEME_OPOST = PRE_MEME + "original_post";
        public static final String MEME_OPOSTER_ID = PRE_MEME + "original_poster_iduser";
        public static final String MEME_OPOSTER_USERNAME = PRE_MEME + "original_poster_username";
        public static final String MEME_STARS_NUM = PRE_MEME + "stars_num";
        public static final String MEME_STARS_STR = PRE_MEME + "stars_str";
        public static final String MEME_STARRED = PRE_MEME + "starred";
        public static final String MEME_COMMENTS_NUM = PRE_MEME + "comments_num";
        public static final String MEME_COMMENTS_STR = PRE_MEME + "comments_str";
        public static final String MEME_REPOSTS_NUM = PRE_MEME + "reposts_num";
        public static final String MEME_REPOSTS_STR = PRE_MEME + "reposts_str";
        public static final String MEME_REPOSTED = PRE_MEME + "reposted";
        public static final String MEME_REPOSTABLE = PRE_MEME + "repostable";
        public static final String MEME_FEED = PRE_MEME + "feed"; // boolean as to whether it's in the feed
        public static final String MEME_HOT = PRE_MEME + "hot"; // boolean as to whether it's in the hot feed

        // users URI
        public static final Uri USERS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();
        public static final String USERS_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_USERS;
        public static final String USERS_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_USERS;

        // user table
        public static final String TABLE_USER = "user";
        public static final String PRE_USER = TABLE_USER + "_";
        public static final String USER_ID = "_id";
        public static final String USER_IDUSER = PRE_USER + "iduser";
        public static final String USER_LINK = PRE_USER + "link";
        public static final String USER_USERNAME = PRE_USER + "username";
        public static final String USER_FIRSTNAME = PRE_USER + "firstName";
        public static final String USER_SURNAME = PRE_USER + "surname";
        public static final String USER_NAME = PRE_USER + "name";
        public static final String USER_PIC = PRE_USER + "pic";
        public static final String USER_FOLLOWING = PRE_USER + "isFollowing";
        public static final String USER_YOU = PRE_USER + "you";

        public static Uri buildMemeUriWithID(long ID){
            return ContentUris.withAppendedId(MEMES_CONTENT_URI,ID);
        }

        public static Uri buildFeedUriWithID(long ID){
            return ContentUris.withAppendedId(FEED_CONTENT_URI,ID);
        }

        public static Uri buildHotUriWithID(long ID){
            return ContentUris.withAppendedId(HOT_CONTENT_URI,ID);
        }

        public static Uri buildUserUriWithID(long ID){
            return ContentUris.withAppendedId(USERS_CONTENT_URI,ID);
        }
    }
}