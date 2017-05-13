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

    /* Inner class that defines the meme table contents */
    public static class Tables implements BaseColumns {
        // memes URI
        public static final Uri MEMES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMES).build();
        public static final String MEMES_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;
        public static final String MEMES_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;

        // meme table
        public static final String TABLE_MEME = "meme";
        public static final String PRE_MEME = "meme_";
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
        public static final String MEME_OPOSTER = PRE_MEME + "original_poster";
        public static final String MEME_STARS_NUM = PRE_MEME + "stars_num";
        public static final String MEME_COMMENTS_NUM = PRE_MEME + "comments_num";
        public static final String MEME_COMMENTS = PRE_MEME + "comments";
        public static final String MEME_REPOSTS_NUM = PRE_MEME + "reposts_num";
        public static final String MEME_REPOSTED = PRE_MEME + "reposted";
        public static final String MEME_REPOSTABLE = PRE_MEME + "repostable";

        // users URI
        public static final Uri USERS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMES).build();
        public static final String USERS_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;
        public static final String USERS_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;

        // user table
        public static final String TABLE_USER = "user";
        public static final String USER_IDUSER = "_id";
        public static final String USER_USERNAME = "username";
        public static final String USER_FIRSTNAME = "firstName";
        public static final String USER_SURNAME = "surname";
        public static final String USER_NAME = "name";
        public static final String USER_PIC = "pic";
        public static final String USER_FOLLOWING = "isFollowing";
        public static final String USER_YOU = "you";

        public static Uri buildMemeUriWithID(long ID){
            return ContentUris.withAppendedId(MEMES_CONTENT_URI,ID);
        }

        public static Uri buildUserUriWithID(long ID){
            return ContentUris.withAppendedId(USERS_CONTENT_URI,ID);
        }
    }
}