package com.garethnunns.memestagram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

import java.util.HashMap;
import java.util.Map;


public class FeedFragment extends Fragment implements LoaderCallbacks<Cursor> {
    // also loader numbers
    public static final int FEED = 1;
    public static final int HOT = 2;

    private static final String ARG_TYPE = "arg_type";

    private int type;

    MemeAdapter adapter;

    private int currentPage = 0;
    private SharedPreferences login;
    private boolean updating = false;
    private boolean firstUpdate = true;
    private boolean end = false; // if they've reached the end

    public static Fragment newInstance(int feedType) {
        if((feedType != FEED) && (feedType != HOT))
            feedType = FEED;
        Fragment frag = new FeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, feedType);
        frag.setArguments(args);
        return frag;
    }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            type = savedInstanceState.getInt(ARG_TYPE);
            firstUpdate = false; // don't clear the cache if it's just a screen rotation
        }
        else {
            Bundle args = getArguments();
            type = args.getInt(ARG_TYPE);
        }

        // init the loader
        getLoaderManager().initLoader(type, null,this);

        adapter = new MemeAdapter(getContext(), null, getActivity(), type, FeedFragment.this, this);

        //bind the adapter to the listview
        ListView lv = (ListView) view.findViewById(R.id.memes_list);
        lv.setAdapter(adapter);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                // update on scroll
                if(firstVisibleItem+visibleItemCount > totalItemCount-2 && totalItemCount!=0)
                    if(!updating)
                        updateFeed(++currentPage,getView());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(ARG_TYPE,type);
        // TODO store scroll position
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateFeed(final int page, View view) {
        if(updating || (end && page > 0)) // prevent lots of web calls
            return;

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.getActiveNetworkInfo() == null
                || !cm.getActiveNetworkInfo().isAvailable()
                || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
            return;
        }

        // clear the cache the first time it's refreshed as the user might have unfollowed/followed more people
        if(firstUpdate) {
            // clear the existing feed
            switch(type) {
                case FEED:
                    getContext().getContentResolver().delete(MemesContract.Tables.FEED_CONTENT_URI,null,null);
                    break;
                case HOT:
                    getContext().getContentResolver().delete(MemesContract.Tables.HOT_CONTENT_URI,null,null);
                    break;
            }
            if(page != 0) updateFeed(0,view);
            firstUpdate = false;
        }

        updating = true;

        final View progress = view.findViewById(R.id.feed_progress);
        showProgress(progress);

        String url;

        switch(type) {
            case FEED:
                url = getString(R.string.api) + "feed";
                break;
            case HOT:
                url = getString(R.string.api) + "hot";
                break;
            default:
                url = getString(R.string.api);
                break;
        }

        Log.i("Updating memes",type+": "+url);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            Boolean success = jsonRes.getBoolean("success");
                            if(success) {
                                // get the memes
                                JSONArray jsonMemes = jsonRes.getJSONArray("memes");

                                if(jsonMemes.length() == 0) {
                                    end = true;
                                    updating = false;
                                    showProgress(progress);
                                    return;
                                }

                                // loop through the memes and store them
                                for (int i = 0; i < jsonMemes.length(); i++) {
                                    Uri added = memestagram.insertMeme(getContext(), jsonMemes.getJSONObject(i));
                                    long id = ContentUris.parseId(added);
                                    switch(type) {
                                        case FEED:
                                            getContext().getContentResolver().insert(MemesContract.Tables.buildFeedUriWithID(id),null);
                                            break;
                                        case HOT:
                                            // this is where it gets a bit messy
                                            // we store the position of the meme in the hot feed
                                            ContentValues position = new ContentValues();
                                            position.put(MemesContract.Tables.MEME_HOT,(i+1)+(page*20));
                                            getContext().getContentResolver().update(MemesContract.Tables.buildHotUriWithID(id),position,null,null);
                                            break;
                                    }
                                }

                                getLoaderManager().restartLoader(type, null, FeedFragment.this);
                            }
                            else
                                Toast.makeText(getContext(), jsonRes.getString("error"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            System.out.println(response);
                            Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                        }
                        updating = false;
                        showProgress(progress);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
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
                params.put("page", String.valueOf(page));
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
        CursorLoader loader = null;
        switch(type) {
            case FEED:
                loader = new CursorLoader(getContext(),MemesContract.Tables.FEED_CONTENT_URI,null,null,null,null);
                break;
            case HOT:
                loader = new CursorLoader(getContext(),MemesContract.Tables.HOT_CONTENT_URI,null,null,null,null);
                break;
        }
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if(firstUpdate)
            updateFeed(currentPage, getView());

        TextView found = (TextView) getView().findViewById(R.id.found);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_share).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateFeed(0,getView());
                break;
        }
        return false;
    }
}
