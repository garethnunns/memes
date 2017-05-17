package com.garethnunns.memestagram;

import android.content.ContentValues;
import android.net.Uri;
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
    MemeAdapter adapter;

    private static final int MEMES_LOADER = 1;
    private int currentPage = 0;
    private SharedPreferences login;
    private boolean updating = false;

    public FeedFragment() {
        // Required empty public constructor
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

        // clear the existing feed
        ContentValues clear = new ContentValues();
        clear.put(MemesContract.Tables.MEME_FEED,0);
        getContext().getContentResolver().update(MemesContract.Tables.MEMES_CONTENT_URI,clear,null,null);

        // init the loader
        getLoaderManager().initLoader(MEMES_LOADER, null,this);

        adapter = new MemeAdapter(getContext(), null, getActivity(), MEMES_LOADER, FeedFragment.this, this);

        updateFeed(currentPage);
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

        //bind the adapter to the listview
        if(savedInstanceState == null) {
            ListView lv = (ListView) view.findViewById(R.id.memes_list);
            lv.setAdapter(adapter);
        }
    }

    public void updateFeed(final int page) {
        // TODO: add some sort of loading sign

        if(updating) // prevent lots of web calls
            return;

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.getActiveNetworkInfo() == null
                || !cm.getActiveNetworkInfo().isAvailable()
                || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;

        String url = getString(R.string.api) + "feed";

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

                                // loop through the memes and store them
                                for (int i = 0; i < jsonMemes.length(); i++) {
                                    Uri added = memestagram.insertMeme(getContext(), jsonMemes.getJSONObject(i));
                                    ContentValues feed = new ContentValues();
                                    feed.put(MemesContract.Tables.MEME_FEED,1);
                                    getContext().getContentResolver().update(added,feed,null,null);
                                }

                                getLoaderManager().restartLoader(MEMES_LOADER, null, FeedFragment.this);
                            }
                            else
                                Toast.makeText(getContext(), jsonRes.getString("error"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            System.out.println(response);
                            Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                        }
                        updating = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                        updating = false;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),MemesContract.Tables.MEMES_CONTENT_URI,null,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

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
        Log.i("menu in fragment","yes, it's being created");
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_share).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateFeed(0);
                break;
        }

        return true;
    }
}
