package com.garethnunns.memestagram;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentUris;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_PROFILE = "arg_profile";
    public static final String ARG_USERNAME = "arg_username";

    private Long iduser;
    private String username;

    private MemeGridAdapter adapter;
    private TextView tUserName;
    private Button follow;

    private int currentPage = 0;
    private SharedPreferences login;
    private boolean updating = false;
    private boolean firstUpdate = true;
    private boolean end = false; // if they've reached the end

    public static final int PROFILE_LOADER = 5;

    public static ProfileFragment newInstance(Long profile, String username) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PROFILE, profile);
        args.putString(ARG_USERNAME, username);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            iduser = savedInstanceState.getLong(ARG_PROFILE);
            username = savedInstanceState.getString(ARG_USERNAME,"Username");
            firstUpdate = false; // don't clear the cache if it's just a screen rotation
        }
        else {
            Bundle args = getArguments();
            iduser = args.getLong(ARG_PROFILE);
            username = args.getString(ARG_USERNAME,"Username");
        }

        tUserName = (TextView) view.findViewById(R.id.profile_username);
        tUserName.setText(username);

        follow = (Button) view.findViewById(R.id.profile_follow);

        // init the loader
        getLoaderManager().initLoader(PROFILE_LOADER, null, this);

        adapter = new MemeGridAdapter(getContext(), null, getActivity(), PROFILE_LOADER, ProfileFragment.this, this);

        // bind the adapter to the gridview
        GridView gv = (GridView) view.findViewById(R.id.profile_grid);
        gv.setAdapter(adapter);

        gv.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                // update on scroll
                if(firstVisibleItem+visibleItemCount > totalItemCount-4 && totalItemCount!=0)
                    if(!updating)
                        updateProfile(++currentPage,getView());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(ARG_PROFILE,iduser);
        savedInstanceState.putString(ARG_USERNAME,username);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateProfile(final int page, final View view) {
        if(updating || end) // prevent lots of web calls
            return;

        if(!memestagram.internetAvailable(getContext())) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if(firstUpdate) {
            // update from the start at the first time
            if(page != 0) updateProfile(0,view);
            firstUpdate = false;
        }

        updating = true;

        final View progress = view.findViewById(R.id.feed_progress);
        showProgress(progress);

        String url = getString(R.string.api)+"profile";

        Log.i("Updating memes","Profile: "+url);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            Boolean success = jsonRes.getBoolean("success");
                            if(success) {
                                // update the stats
                                JSONObject jsonStats = jsonRes.getJSONObject("stats");

                                TextView tPostsNum = (TextView) view.findViewById(R.id.profile_posts_num);
                                tPostsNum.setText(jsonStats.getString("posts"));

                                TextView tPostsStr = (TextView) view.findViewById(R.id.profile_posts_str);
                                tPostsStr.setText(jsonStats.getString("posts-str"));

                                TextView tFollowersNum = (TextView) view.findViewById(R.id.profile_followers_num);
                                tFollowersNum.setText(jsonStats.getString("followers"));

                                TextView tFollowersStr = (TextView) view.findViewById(R.id.profile_followers_str);
                                tFollowersStr.setText(jsonStats.getString("followers-str"));

                                TextView tFollowingNum = (TextView) view.findViewById(R.id.profile_following_num);
                                tFollowingNum.setText(jsonStats.getString("following"));

                                TextView tFollowingStr = (TextView) view.findViewById(R.id.profile_following_str);
                                tFollowingStr.setText(jsonStats.getString("following-str"));

                                TextView tStarsNum = (TextView) view.findViewById(R.id.profile_stars_num);
                                tStarsNum.setText(jsonStats.getString("stars"));

                                TextView tStarsStr = (TextView) view.findViewById(R.id.profile_stars_str);
                                tStarsStr.setText(jsonStats.getString("stars-str"));

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

                                getLoaderManager().restartLoader(PROFILE_LOADER, null, ProfileFragment.this);
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
                params.put("id", String.valueOf(iduser));
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
        CursorLoader loader = new CursorLoader(getContext(), MemesContract.Tables.buildProfileUriWithID(iduser),null,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);

        TextView found = (TextView) getView().findViewById(R.id.found);
        if((cursor == null) || (cursor.getCount()==0))
            found.setText(R.string.error_no_memes);
        else {
            found.setText("");

            cursor.moveToFirst();
            final String ppURL = cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_PIC));
            final ImageView pp = (ImageView) getView().findViewById(R.id.profile_pp);
            Picasso.with(getContext())
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
                            Picasso.with(getContext())
                                    .load(ppURL)
                                    .error(R.drawable.pp)
                                    .into(pp);
                        }
                    });

            tUserName.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_USERNAME)));

            TextView tName = (TextView) getView().findViewById(R.id.profile_name);
            tName.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_NAME)));

            if(cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_YOU)) == 1) {
                follow.setText("Settings");
            }
            else {
                follow.setText(cursor.getInt(cursor.getColumnIndexOrThrow(MemesContract.Tables.USER_FOLLOWING)) == 1 ? getContext().getString(R.string.Unfollow) : getContext().getString(R.string.Follow));
            }
        }

        if(firstUpdate)
            updateProfile(currentPage, getView());

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
        // todo: set share provider
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // todo: fix refresh button refreshing in all windows
        // could test for bottom bar selected
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //updateProfile(0,getView());
                break;
        }
        return false;
    }
}
