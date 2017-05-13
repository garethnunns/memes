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
        // TODO: do a multitable select in the CP

        // http://memes-store.garethnunns.com/profile/user/1.png

        // load the user's profile picture

        // load the full image
        final String ppURL = "http://memes-store.garethnunns.com/profile/user/1.png";
        final ImageView pp = (ImageView) view.findViewById(R.id.meme_pp);
        Picasso.with(context)
                .load(ppURL)
                .networkPolicy(NetworkPolicy.OFFLINE) // try use the cache
                .placeholder(R.drawable.pp)
                .into(pp, new Callback() {
                    @Override
                    public void onSuccess() { // look in the cache
                        Log.v("Picasso","Image found in the cache - " + ppURL);
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(ppURL)
                                .error(R.drawable.pp)
                                .into(pp);
                    }
                });

        TextView username = (TextView) view.findViewById(R.id.meme_username);
        username.setText("Username");

        TextView posted = (TextView) view.findViewById(R.id.meme_posted);
        posted.setText("Posted");

        TextView by = (TextView) view.findViewById(R.id.meme_by);
        by.setText(" by ");

        TextView name = (TextView) view.findViewById(R.id.meme_name);
        name.setText("Gareth Nunns");

        TextView ago = (TextView) view.findViewById(R.id.meme_ago);
        ago.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_AGO)));

        TextView caption = (TextView) view.findViewById(R.id.meme_caption);
        caption.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_CAPTION)));

        // load the full image
        final String full = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_FULL));
        final ImageView image = (ImageView) view.findViewById(R.id.meme_image);
        Picasso.with(context)
                .load(full)
                .networkPolicy(NetworkPolicy.OFFLINE) // try use the cache
                .placeholder(R.drawable.loading)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() { // look in the cache
                        Log.v("Picasso","Image found in the cache - " + full);
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(full)
                                .error(R.drawable.notfound)
                                .into(image);
                    }
                });
    }
}
