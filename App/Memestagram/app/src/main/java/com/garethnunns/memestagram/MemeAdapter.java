package com.garethnunns.memestagram;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
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
 * output the memes in a list in the style of meme_item.xml
 */

public class MemeAdapter extends CursorAdapter {
    private Activity activity;

    public MemeAdapter(Context context, Cursor cursor, Activity a) {
        super(context, cursor, 0);
        activity = a;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.meme_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // load the user's profile picture
        final String ppURL = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_PIC));
        final ImageView pp = (ImageView) view.findViewById(R.id.meme_pp);
        Picasso.with(context)
                .load(ppURL)
                .networkPolicy(NetworkPolicy.OFFLINE) // try use the cache
                .placeholder(R.drawable.pp)
                .into(pp, new Callback() {
                    @Override
                    public void onSuccess() { // look in the cache
                        Log.i("Picasso", "Image found in the cache - " + ppURL);
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
        username.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_USERNAME)));

        Long o_post = cursor.getLong(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_OPOST));

        TextView posted = (TextView) view.findViewById(R.id.meme_posted);
        TextView name = (TextView) view.findViewById(R.id.meme_name);
        if (o_post == 0) {
            posted.setText(R.string.posted);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_NAME)));
        } else {
            posted.setText(R.string.originally_posted);
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_OPOSTER_USERNAME)));
        }

        TextView by = (TextView) view.findViewById(R.id.meme_by);
        by.setText(" " + context.getString(R.string.by) + " ");

        TextView ago = (TextView) view.findViewById(R.id.meme_ago);
        ago.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_AGO)));

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
                        Log.i("Picasso", "Image found in the cache - " + full);
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

        TextView caption = (TextView) view.findViewById(R.id.meme_caption);
        String strCaption = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_CAPTION));
        if (TextUtils.isEmpty(strCaption))
            caption.setVisibility(View.GONE);
        else {
            caption.setVisibility(View.VISIBLE);
            caption.setText(strCaption);
        }

        // TODO: make image do something on tap

        TextView comments_num = (TextView) view.findViewById(R.id.meme_comments_num);
        comments_num.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_COMMENTS_NUM)) +
                " " + cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_COMMENTS_STR)));

        final Activity theActivity = activity;

        final ImageView ic_stars = (ImageView) view.findViewById(R.id.meme_ic_star);
        if (cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_STARRED)) == 1)
            ic_stars.setImageResource(R.drawable.blue_star_full);
        else
            ic_stars.setImageResource(R.drawable.grey_star_empty);

        ic_stars.setClickable(true);
        ic_stars.setTag(cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_IDMEME)));
        ic_stars.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        memestagram.star(context, theActivity, 1, (Integer) v.getTag());
                    }
                }
        );

        TextView stars_num = (TextView) view.findViewById(R.id.meme_stars_num);
        stars_num.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_STARS_NUM)) +
                " " + cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_STARS_STR)));

        ImageView ic_reposts = (ImageView) view.findViewById(R.id.meme_ic_repost);
        if (cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_REPOSTED)) == 1)
            ic_reposts.setImageResource(R.drawable.blue_repost);

        TextView reposts_num = (TextView) view.findViewById(R.id.meme_reposts_num);
        reposts_num.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_REPOSTS_NUM)) +
                " " + cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_REPOSTS_STR)));
    }
}