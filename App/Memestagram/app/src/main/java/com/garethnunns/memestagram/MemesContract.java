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

    /* Inner class that defines the meme table contents */
    public static class Tables implements BaseColumns {
        // db URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMES).build();
        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"+CONTENT_AUTHORITY+"/"+PATH_MEMES;

        // meme table
        public static final String TABLE_MEME = "meme";
        public static final String MEME_IDMEME = "idmeme";
        public static final String MEME_IDUSER = "iduser";
        public static final String MEME_THUMB = "thumb";
        public static final String MEME_FULL = "full";
        public static final String MEME_LINK = "link";
        public static final String MEME_EPOCH = "epoch";
        public static final String MEME_AGO = "ago";
        public static final String MEME_CAPTION = "caption";
        public static final String MEME_LAT = "lat";
        public static final String MEME_LONG = "long";
        public static final String MEME_OPOST = "original_post";
        public static final String MEME_OPOSTER = "original_poster";
        public static final String MEME_STARS_NUM = "stars-num";
        public static final String MEME_COMMENTS_NUM = "comments-num";
        public static final String MEME_COMMENTS = "comments";
        public static final String MEME_REPOSTS_NUM = "reposts-num";
        public static final String MEME_REPOSTED = "reposted";
        public static final String MEME_REPOSTABLE = "repostable";

        // user table
        public static final String TABLE_USER = "user";
        public static final String USER_IDUSER = "iduser";
        public static final String USER_USERNAME = "username";
        public static final String USER_FIRSTNAME = "firstName";
        public static final String USER_SURNAME = "surname";
        public static final String USER_NAME = "name";
        public static final String USER_PIC = "pic";
        public static final String USER_FOLLOWING = "isFollowing";
        public static final String USER_YOU = "you";

        public static Uri buildMemesUriWithID(long ID){
            return ContentUris.withAppendedId(CONTENT_URI,ID);
        }
    }
}