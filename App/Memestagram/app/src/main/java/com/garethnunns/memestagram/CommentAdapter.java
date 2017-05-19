package com.garethnunns.memestagram;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by gareth on 19/05/2017.
 */

public class CommentAdapter extends ArrayAdapter {
    private Context context;

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        final Comment comment = (Comment) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.comment, parent, false);
        }

        // go to user event
        View.OnClickListener clickCommenter = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("click", "Username: " + comment.username + " user: " + comment.iduser);
                FragmentManager fm = ((FragmentActivity) ((Activity) context)).getSupportFragmentManager();
                Fragment frag = ProfileFragment.newInstance(comment.iduser, comment.username);
                String fragTitle = "User "+comment.iduser;
                Fragment already = fm.findFragmentByTag(fragTitle);
                if(already != null) frag = already;
                fm.beginTransaction()
                        .replace(R.id.container, frag, fragTitle)
                        .addToBackStack(fragTitle)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        };

        // get the views and fill them
        final String ppURL = comment.pp;
        final ImageView pp = (ImageView) view.findViewById(R.id.comment_pp);
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
        pp.setOnClickListener(clickCommenter);

        TextView tUsername = (TextView) view.findViewById(R.id.comment_username);
        tUsername.setText(comment.username);
        tUsername.setOnClickListener(clickCommenter);

        TextView tAgo = (TextView) view.findViewById(R.id.comment_ago);
        tAgo.setText(comment.ago);

        TextView tComment = (TextView) view.findViewById(R.id.comment_comment);
        tComment.setText(comment.comment);

        // Return the completed view to render on screen
        return view;
    }
}
