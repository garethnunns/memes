package com.garethnunns.memestagram;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by gareth on 18/05/2017.
 * for outputting a meme thumbnail in a grid
 */

public class MemeGridAdapter extends CursorAdapter {
    private Activity activity;
    private Integer loader;
    private LoaderManager.LoaderCallbacks<?> cb;
    private Fragment frag;

    public MemeGridAdapter(Context context, Cursor cursor, Activity a, Integer l, LoaderManager.LoaderCallbacks<?> callbacks, Fragment fragment) {
        super(context, cursor, 0);
        activity = a;
        loader = l;
        cb = callbacks;
        frag = fragment;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.meme_grid_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // load the thumbnail image
        final String thumb = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_THUMB));
        final ImageView image = (ImageView) view.findViewById(R.id.meme_grid_thumb);
        Picasso.with(context)
                .load(thumb)
                .networkPolicy(NetworkPolicy.OFFLINE) // try use the cache
                .placeholder(R.drawable.loading)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() { // look in the cache
                        Log.i("Picasso", "Image found in the cache - " + thumb);
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(thumb)
                                .error(R.drawable.notfound)
                                .into(image);
                    }
                });

        final int idmeme = cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_IDMEME));
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("click", "Going to meme " + idmeme);
                String fragTitle = "Meme "+idmeme;
                FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
                Fragment frag = MemeFragment.newInstance(idmeme);
                Fragment already = fm.findFragmentByTag(fragTitle);
                if(already != null) frag = already;
                fm.beginTransaction()
                        .replace(R.id.container, frag, fragTitle)
                        .addToBackStack(fragTitle)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        });
    }
}
