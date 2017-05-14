package com.garethnunns.memestagram;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    MemeAdapter adapter;

    //The Loader ID, defined by developers, a loader is registered with the LoaderManager using this ID
    private static final int MEMES_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Memestagram", "Welcome");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the loader
        getLoaderManager().initLoader(MEMES_LOADER, null, this);

        adapter = new MemeAdapter(getApplicationContext(),null);
        //bind the adapter to the listview
        ListView lv = (ListView) findViewById(R.id.memes_list);
        lv.setAdapter(adapter);

        ContentValues meme = new ContentValues();
        meme.put(MemesContract.Tables.MEME_IDMEME,"1");
        meme.put(MemesContract.Tables.MEME_IDUSER,"1");
        meme.put(MemesContract.Tables.MEME_THUMB,"http://memes-store.garethnunns.com/thumb/400/1.jpg");
        meme.put(MemesContract.Tables.MEME_FULL,"http://memes-store.garethnunns.com/full/1000/1.jpg");
        meme.put(MemesContract.Tables.MEME_LINK,"http://memes.garethnunns.com/garethnunns/1");
        meme.put(MemesContract.Tables.MEME_EPOCH,"1493065722");
        meme.put(MemesContract.Tables.MEME_AGO,"2w");
        meme.put(MemesContract.Tables.MEME_CAPTION,"Hello world");
        meme.put(MemesContract.Tables.MEME_STARS_NUM,"3");
        meme.put(MemesContract.Tables.MEME_COMMENTS_NUM,"1");
        meme.put(MemesContract.Tables.MEME_REPOSTS_NUM,"1");
        meme.put(MemesContract.Tables.MEME_REPOSTED,"0");
        meme.put(MemesContract.Tables.MEME_REPOSTABLE,"0");

        getContentResolver().insert(MemesContract.Tables.MEMES_CONTENT_URI,meme);

        ContentValues user = new ContentValues();
        user.put(MemesContract.Tables.USER_IDUSER,"1");
        user.put(MemesContract.Tables.USER_USERNAME,"garethnunns");
        user.put(MemesContract.Tables.USER_FIRSTNAME,"Gareth");
        user.put(MemesContract.Tables.USER_SURNAME,"Nunns");
        user.put(MemesContract.Tables.USER_NAME,"Gareth Nunns");
        user.put(MemesContract.Tables.USER_PIC,"http://memes-store.garethnunns.com/profile/user/1.png");
        user.put(MemesContract.Tables.USER_FOLLOWING,"0");
        user.put(MemesContract.Tables.USER_YOU,"1");

        getContentResolver().insert(MemesContract.Tables.USERS_CONTENT_URI,user);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = {
                MemesContract.Tables.MEME_ID,
                MemesContract.Tables.MEME_IDMEME,
                MemesContract.Tables.MEME_IDUSER,
                MemesContract.Tables.MEME_CAPTION,
                MemesContract.Tables.MEME_AGO,
                MemesContract.Tables.MEME_FULL
        };
        CursorLoader loader = new CursorLoader(this,MemesContract.Tables.MEMES_CONTENT_URI,columns,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        TextView found = (TextView) findViewById(R.id.found);
        if((data == null) || (data.getCount()==0))
            found.setText(R.string.error_no_memes);
        else
            found.setText("");

        Log.i("Loader","onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.i("loader","onLoaderReset");
    }
}
