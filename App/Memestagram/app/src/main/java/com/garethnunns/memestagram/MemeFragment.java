package com.garethnunns.memestagram;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_MEME = "arg_meme";
    private int meme;

    private int currentPage = 0;
    private SharedPreferences login;
    private boolean updating = false;
    private boolean firstUpdate = true;

    public static final int MEME_LOADER = 7;
    private MemeAdapter adapter;
    ListView lv;

    private ArrayList<Comment> comments;
    private CommentAdapter commentAdapter;
    ListView clv;

    private ShareActionProvider mShareActionProvider;

    public static MemeFragment newInstance(int meme) {
        MemeFragment fragment = new MemeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEME, meme);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(!memestagram.loggedIn(getContext()))
            memestagram.logout(getContext(),getActivity());

        login = memestagram.getLogin(getContext());
    }

    @Override
    public void onViewStateRestored(Bundle inState) {
        super.onViewStateRestored(inState);
        updateMeme(0,getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meme, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            meme = savedInstanceState.getInt(ARG_MEME);
            firstUpdate = false; // don't look it up again if it's just a rotation
        }
        else {
            Bundle args = getArguments();
            meme = args.getInt(ARG_MEME);
        }

        // init the loader
        getLoaderManager().initLoader(MEME_LOADER, null,this);

        adapter = new MemeAdapter(getContext(), null, getActivity(), MEME_LOADER, MemeFragment.this, this);

        //bind the adapter to the listview
        lv = (ListView) view.findViewById(R.id.meme_frag_meme);
        lv.setAdapter(adapter);

        // comments section
        comments = new ArrayList<Comment>();
        commentAdapter = new CommentAdapter(getContext(),comments);
        clv = (ListView) view.findViewById(R.id.meme_frag_comments);
        clv.setAdapter(commentAdapter);
        memestagram.setListViewHeightBasedOnChildren(clv);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(ARG_MEME,meme);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateMeme(final int page, final View view) {
        if(updating) // prevent lots of web calls
            return;

        if(!memestagram.internetAvailable(getContext())) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if(firstUpdate) {
            // update from the start at the first time
            if(page != 0) updateMeme(0,view);
            firstUpdate = false;
        }

        updating = true;

        final View progress = view.findViewById(R.id.feed_progress);
        showProgress(progress);

        String url = getString(R.string.api)+"meme";

        Log.i("Updating memes",ARG_MEME+": "+url);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            Boolean success = jsonRes.getBoolean("success");
                            if(success) {
                                // update the meme
                                JSONObject jsonMeme = jsonRes.getJSONObject("meme");
                                memestagram.insertMeme(getContext(), jsonMeme);

                                if(jsonMeme.getInt("comments-num")>0) {
                                    // there are some comments on the meme
                                    JSONArray jsonComments = jsonMeme.getJSONArray("comments");

                                    for (int i = 0; i < jsonComments.length(); i++) {
                                        JSONObject jsonComment = jsonComments.getJSONObject(i);

                                        JSONObject jsonCommenter = jsonComment.getJSONObject("commenter");

                                        // whilst we're here we may as well store all of the users in the database
                                        memestagram.insertUser(getContext(), jsonCommenter);

                                        Comment comment = new Comment(
                                                jsonCommenter.getLong("iduser"),
                                                jsonCommenter.getString("pic"),
                                                jsonCommenter.getString("username"),
                                                jsonComment.getJSONObject("time").getString("ago"),
                                                jsonComment.getString("comment")
                                        );

                                        comments.add(comment);
                                    }
                                }

                                getLoaderManager().restartLoader(MEME_LOADER, null, MemeFragment.this);
                                commentAdapter.notifyDataSetChanged();
                                memestagram.setListViewHeightBasedOnChildren(clv);
                            }
                            else
                                Toast.makeText(getContext(), jsonRes.getString("error"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            System.out.println(response);
                            Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                        }
                        updating = false;
                        showProgress(progress);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                        updating = false;
                        showProgress(progress);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("key", login.getString("key",""));
                params.put("id", String.valueOf(meme));
                params.put("limitComments", "0");
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(postRequest);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final View progressView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    updating ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),MemesContract.Tables.buildMemeUriWithID(meme),null,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        memestagram.setListViewHeightBasedOnChildren(lv);

        if(firstUpdate)
            updateMeme(currentPage, getView());

        TextView found = (TextView) getView().findViewById(R.id.found);
        if((cursor == null) || (cursor.getCount()==0))
            found.setText(R.string.error_no_memes);
        else {
            found.setText("");

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.MEME_LINK)));
            mShareActionProvider.setShareIntent(shareIntent);
        }

        Log.i("Loader","onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.i("loader","onLoaderReset");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(getUserVisibleHint())
            switch (item.getItemId()) {
                case R.id.action_refresh:
                    updateMeme(0,getView());
                    break;
            }
        return false;
    }
}