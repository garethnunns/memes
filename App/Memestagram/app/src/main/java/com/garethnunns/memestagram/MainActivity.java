package com.garethnunns.memestagram;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    MemeAdapter adapter;

    //The Loader ID, defined by developers, a loader is registered with the LoaderManager using this ID
    private static final int MEMES_LOADER = 1;

    private int page = 0;
    private SharedPreferences login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Memestagram", "Welcome");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!memestagram.loggedIn(getApplicationContext()))
            memestagram.logout(getApplicationContext());

        login = memestagram.getLogin(getApplicationContext());

        // init the loader
        getLoaderManager().initLoader(MEMES_LOADER, null, this);

        adapter = new MemeAdapter(getApplicationContext(),null);
        //bind the adapter to the listview
        ListView lv = (ListView) findViewById(R.id.memes_list);
        lv.setAdapter(adapter);

        updateFeed(page);

        // TODO: implement logout button
    }

    public void updateFeed(final int page) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.getActiveNetworkInfo() == null
                || !cm.getActiveNetworkInfo().isAvailable()
                || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
            return;
        }


        String url = getString(R.string.api) + "feed";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            Boolean success = jsonRes.getBoolean("success");
                            if(success) {
                                //String key = jsonRes.getString("key");
                                //Integer id = jsonRes.getInt("user");

                                // get the memes
                                JSONArray jsonMemes = jsonRes.getJSONArray("memes");

                                // loop through the memes
                                for (int i = 0; i < jsonMemes.length(); i++) {
                                    // get this meme object
                                    JSONObject jsonMeme = jsonMemes.getJSONObject(i);

                                    // store the users to start off with

                                    // retrieve the poster object
                                    JSONObject jsonPoster = jsonMeme.getJSONObject("poster");

                                    ContentValues poster = new ContentValues();
                                    poster.put(MemesContract.Tables.USER_IDUSER,jsonPoster.getString("iduser"));
                                    poster.put(MemesContract.Tables.USER_LINK,jsonPoster.getString("link"));
                                    poster.put(MemesContract.Tables.USER_USERNAME,jsonPoster.getString("username"));
                                    poster.put(MemesContract.Tables.USER_FIRSTNAME,jsonPoster.getString("firstName"));
                                    poster.put(MemesContract.Tables.USER_SURNAME,jsonPoster.getString("surname"));
                                    poster.put(MemesContract.Tables.USER_NAME,jsonPoster.getString("name"));
                                    poster.put(MemesContract.Tables.USER_PIC,jsonPoster.getString("pic"));
                                    poster.put(MemesContract.Tables.USER_FOLLOWING,jsonPoster.getString("isFollowing"));
                                    poster.put(MemesContract.Tables.USER_YOU,jsonPoster.getString("you"));

                                    getContentResolver().insert(MemesContract.Tables.USERS_CONTENT_URI,poster);

                                    // TODO: store the original poster

                                    // then store the meme
                                    ContentValues meme = new ContentValues();
                                    meme.put(MemesContract.Tables.MEME_IDMEME,jsonMeme.getString("idmeme"));
                                    meme.put(MemesContract.Tables.MEME_IDUSER,jsonPoster.getString("iduser"));

                                    // get the images object
                                    JSONObject jsonImages = jsonMeme.getJSONObject("images");
                                    meme.put(MemesContract.Tables.MEME_THUMB,jsonImages.getString("thumb"));
                                    meme.put(MemesContract.Tables.MEME_FULL,jsonImages.getString("full"));

                                    meme.put(MemesContract.Tables.MEME_LINK,jsonMeme.getString("link"));

                                    // get the time object
                                    JSONObject jsonTime = jsonMeme.getJSONObject("time");
                                    meme.put(MemesContract.Tables.MEME_EPOCH,jsonTime.getString("epoch"));
                                    meme.put(MemesContract.Tables.MEME_AGO,jsonTime.getString("ago"));

                                    meme.put(MemesContract.Tables.MEME_CAPTION,jsonMeme.getString("caption"));
                                    meme.put(MemesContract.Tables.MEME_STARS_NUM,jsonMeme.getString("stars-num"));
                                    meme.put(MemesContract.Tables.MEME_COMMENTS_NUM,jsonMeme.getString("comments-num"));
                                    meme.put(MemesContract.Tables.MEME_REPOSTS_NUM,jsonMeme.getString("reposts-num"));
                                    meme.put(MemesContract.Tables.MEME_REPOSTED,jsonMeme.getString("reposted"));
                                    meme.put(MemesContract.Tables.MEME_REPOSTABLE,jsonMeme.getString("repostable"));

                                    getContentResolver().insert(MemesContract.Tables.MEMES_CONTENT_URI,meme);

                                    /*
                                    public static final String MEME_LAT = PRE_MEME + "lat";
                                    public static final String MEME_LONG = PRE_MEME + "long";
                                    public static final String MEME_OPOST = PRE_MEME + "original_post";
                                    public static final String MEME_OPOSTER_ID = PRE_MEME + "original_poster_iduser";
                                    public static final String MEME_OPOSTER_USERNAME = PRE_MEME + "original_poster_username";
                                     */
                                }

                                getLoaderManager().restartLoader(MEMES_LOADER, null, MainActivity.this);
                            }
                            else
                                Toast.makeText(getApplicationContext(), jsonRes.getString("error"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            System.out.println(response);
                            Toast.makeText(getApplicationContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_internal), Toast.LENGTH_LONG).show();
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
        Volley.newRequestQueue(this).add(postRequest);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] columns = {
                MemesContract.Tables.MEME_ID,
                MemesContract.Tables.MEME_IDMEME,
                MemesContract.Tables.MEME_IDUSER,
                MemesContract.Tables.MEME_CAPTION,
                MemesContract.Tables.MEME_AGO,
                MemesContract.Tables.MEME_FULL
        };
        CursorLoader loader = new CursorLoader(this,MemesContract.Tables.MEMES_CONTENT_URI,columns,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        TextView found = (TextView) findViewById(R.id.found);
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
}
