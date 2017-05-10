package com.garethnunns.memestagram;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter adapter;

    //The Loader ID, defined by developers, a loader is registered with the LoaderManager using this ID
    private static final int MEMES_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the loader
        getLoaderManager().initLoader(MEMES_LOADER, null, this);

        //create a SimpleCursor adapter
        adapter = new SimpleCursorAdapter(
                getApplicationContext(),//the context in which it will operate
                R.layout.meme_item, //the list item layout
                null,//database cursor
                new String[] {MemesContract.Tables.MEME_CAPTION},//the source data
                new int[]{R.id.meme_full},//The TextViews that will display the data
                0//flags, set it to 0 because CursorLoader registers it for us
        );
        //bind the adapter to the listview
        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(adapter);

        // test adding one

        ContentValues values = new ContentValues();
        values.put(MemesContract.Tables.MEME_CAPTION,"hello");
        values.put(MemesContract.Tables.MEME_IDMEME,1);

        getContentResolver().insert(MemesContract.Tables.MEMES_CONTENT_URI,values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = {
                MemesContract.Tables.MEME_IDMEME,
                MemesContract.Tables.MEME_CAPTION,
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
