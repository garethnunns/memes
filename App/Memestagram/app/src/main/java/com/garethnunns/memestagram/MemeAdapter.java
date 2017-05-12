package com.garethnunns.memestagram;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by gareth on 12/05/2017.
 */

public class MemeAdapter extends CursorAdapter {
    public MemeAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.meme_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView caption = (TextView) view.findViewById(R.id.meme_caption);
        //final String cap = cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_CAPTION);
        caption.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_CAPTION)));

        final String full = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_FULL));
        ImageView test = (ImageView) view.findViewById(R.id.meme_image);
        Picasso.with(context)
                .load(full)
                .networkPolicy(NetworkPolicy.OFFLINE) // try use the cache
                .into(test, new Callback() {
                    @Override
                    public void onSuccess() { // look in the cache
                        Log.v("Picasso","Image found in the cache");
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        ImageView test = (ImageView) view.findViewById(R.id.meme_image);
                        Picasso.with(context)
                                .load(full)
                                .error(R.drawable.notfound)
                                .into(test, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });
    }
}
