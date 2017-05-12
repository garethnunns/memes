package com.garethnunns.memestagram;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    MemeAdapter adapter;

    //The Loader ID, defined by developers, a loader is registered with the LoaderManager using this ID
    private static final int MEMES_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the loader
        getLoaderManager().initLoader(MEMES_LOADER, null, this);

        adapter = new MemeAdapter(getApplicationContext(),null);
        //bind the adapter to the listview
        ListView lv = (ListView) findViewById(R.id.memes_list);
        lv.setAdapter(adapter);

        // test adding one
        getContentResolver().delete(MemesContract.Tables.MEMES_CONTENT_URI,null,null); // clear the whole table first

        ContentValues values = new ContentValues();
        values.put(MemesContract.Tables.MEME_IDMEME,"4");
        values.put(MemesContract.Tables.MEME_IDUSER,"1");
        values.put(MemesContract.Tables.MEME_THUMB,"http://memes-store.garethnunns.com/thumb/400/1.jpg");
        values.put(MemesContract.Tables.MEME_FULL,"http://memes-store.garethnunns.com/full/1000/1.jpg");
        values.put(MemesContract.Tables.MEME_LINK,"http://memes.garethnunns.com/garethnunns/1");
        values.put(MemesContract.Tables.MEME_EPOCH,"1493065722");
        values.put(MemesContract.Tables.MEME_AGO,"2w");
        values.put(MemesContract.Tables.MEME_CAPTION,"Hello world");
        values.put(MemesContract.Tables.MEME_STARS_NUM,"3");
        values.put(MemesContract.Tables.MEME_COMMENTS_NUM,"1");
        //values.put(MemesContract.Tables.MEME_COMMENTS,"Hello. World.");
        values.put(MemesContract.Tables.MEME_REPOSTS_NUM,"1");
        values.put(MemesContract.Tables.MEME_REPOSTED,"0");
        values.put(MemesContract.Tables.MEME_REPOSTABLE,"0");

        getContentResolver().insert(MemesContract.Tables.MEMES_CONTENT_URI,values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = {
                MemesContract.Tables.MEME_IDMEME,
                MemesContract.Tables.MEME_IDUSER,
                MemesContract.Tables.MEME_CAPTION,
                MemesContract.Tables.MEME_FULL
        };
        CursorLoader loader = new CursorLoader(this,MemesContract.Tables.MEMES_CONTENT_URI,columns,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        Log.i("Loader","onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.i("loader","onLoaderReset");
    }
}
