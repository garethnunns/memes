package com.garethnunns.memestagram;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StarredFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StarredFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    MemeGridAdapter adapter;

    private int currentPage = 0;
    private SharedPreferences login;
    private boolean updating = false;
    private boolean firstUpdate = true;
    private boolean end = false; // if they've reached the end

    public static final int STARRED_LOADER = 6;

    public static StarredFragment newInstance() {
        StarredFragment fragment = new StarredFragment();
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
        return inflater.inflate(R.layout.fragment_starred, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // init the loader
        getLoaderManager().initLoader(STARRED_LOADER, null,this);

        adapter = new MemeGridAdapter(getContext(), null, getActivity(), STARRED_LOADER, StarredFragment.this, this);

        // bind the adapter to the gridview
        GridView gv = (GridView) view.findViewById(R.id.starred_grid);
        gv.setAdapter(adapter);

        gv.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                // update on scroll
                if(firstVisibleItem+visibleItemCount > totalItemCount-2 && totalItemCount!=0)
                    if(!updating)
                        updateStarred(++currentPage,getView());
            }
        });
    }

    public void updateStarred(final int page, View view) {
        if(updating || end) // prevent lots of web calls
            return;

        if(!memestagram.internetAvailable(getContext())) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        // clear the cache the first time it's refreshed as the user might have unfollowed/followed more people
        if(firstUpdate) {
            // clear the existing feed
            if(page != 0) updateStarred(0,view);
            firstUpdate = false;
        }

        updating = true;

        final View progress = view.findViewById(R.id.feed_progress);
        showProgress(progress);

        String url = getString(R.string.api)+"starred";

        Log.i("Updating memes","Starred: "+url);

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
                                for (int i = 0; i < jsonMemes.length(); i++)
                                    memestagram.insertMeme(getContext(), jsonMemes.getJSONObject(i));

                                getLoaderManager().restartLoader(STARRED_LOADER, null, StarredFragment.this);
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
        CursorLoader loader = new CursorLoader(getContext(),MemesContract.Tables.STARRED_CONTENT_URI,null,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if(firstUpdate)
            updateStarred(currentPage, getView());

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
                updateStarred(0,getView());
                break;
        }
        return false;
    }
}
