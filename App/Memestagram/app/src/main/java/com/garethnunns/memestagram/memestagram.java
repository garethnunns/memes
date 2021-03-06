package com.garethnunns.memestagram;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by gareth on 15/05/2017.
 * some functions that are used in all files pretty much
 */

class memestagram {
    public static SharedPreferences getLogin(Context context) {
        return context.getSharedPreferences("login",MODE_PRIVATE);
    }

    public static boolean loggedIn(Context context) {
        // checks whether the user is logged in
        // (only by checking the variables are set, not verifying them)
        final SharedPreferences login = getLogin(context);
        if(!login.contains("username") || !login.contains("password") || !login.contains("key") || !login.contains("iduser"))
            return false;
        return true;
    }

    public static void logout(Context context, Activity activity) {
        // logs the user out

        // removes their login details
        SharedPreferences login = getLogin(context);
        login.edit().clear().commit();

        // remove all the memes and users as this data is specific to that user
        MemesDBHelper theDBHelper = new MemesDBHelper(context);
        theDBHelper.clearMeme();
        theDBHelper.clearUser();

        Intent gologin = new Intent(context,LoginActivity.class);
        gologin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(gologin);
        activity.finish();
    }

    public static boolean internetAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static Uri insertUser(Context context, JSONObject jsonUser) throws JSONException {
        // inserts a user into the database
        ContentValues user = new ContentValues();
        user.put(MemesContract.Tables.USER_IDUSER, jsonUser.getString("iduser"));
        user.put(MemesContract.Tables.USER_LINK, jsonUser.getString("link"));
        user.put(MemesContract.Tables.USER_USERNAME, jsonUser.getString("username"));
        user.put(MemesContract.Tables.USER_FIRSTNAME, jsonUser.getString("firstName"));
        user.put(MemesContract.Tables.USER_SURNAME, jsonUser.getString("surname"));
        user.put(MemesContract.Tables.USER_NAME, jsonUser.getString("name"));
        user.put(MemesContract.Tables.USER_PIC, jsonUser.getString("pic"));
        user.put(MemesContract.Tables.USER_FOLLOWING, jsonUser.getString("isFollowing"));
        user.put(MemesContract.Tables.USER_YOU, jsonUser.getString("you"));

        return context.getContentResolver().insert(MemesContract.Tables.USERS_CONTENT_URI, user);
    }

    public static Uri insertMeme(Context context, JSONObject jsonMeme) throws JSONException {
        // inserts a meme into the databse
        ContentValues meme = new ContentValues();

        JSONObject jsonPoster = jsonMeme.getJSONObject("poster");

        // store the poster of this meme
        insertUser(context,jsonPoster);

        // see if it is an original post
        Object original = jsonMeme.get("original");
        if(original instanceof Boolean) {
            // ideally your API wouldn't return different variable types...
            // this might get fixed in a later API revision
            // if it's in this block then it's an original post
            // e.g. not a repost
            meme.put(MemesContract.Tables.MEME_OPOST,0);
        }
        else {
            // a repost
            JSONObject jsonOriginal = jsonMeme.getJSONObject("original");
            JSONObject jsonOPoster = jsonOriginal.getJSONObject("poster");

            memestagram.insertUser(context,jsonOPoster);

            meme.put(MemesContract.Tables.MEME_OPOST,jsonOriginal.getString("idmeme"));
            meme.put(MemesContract.Tables.MEME_OPOSTER_ID,jsonOPoster.getString("iduser"));
            meme.put(MemesContract.Tables.MEME_OPOSTER_USERNAME,jsonOPoster.getString("username"));
        }

        // then store the meme
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
        meme.put(MemesContract.Tables.MEME_STARS_STR,jsonMeme.getString("stars-str"));
        meme.put(MemesContract.Tables.MEME_STARRED,jsonMeme.getString("starred"));

        meme.put(MemesContract.Tables.MEME_COMMENTS_NUM,jsonMeme.getString("comments-num"));
        meme.put(MemesContract.Tables.MEME_COMMENTS_STR,jsonMeme.getString("comments-str"));

        meme.put(MemesContract.Tables.MEME_REPOSTS_NUM,jsonMeme.getString("reposts-num"));
        meme.put(MemesContract.Tables.MEME_REPOSTS_STR,jsonMeme.getString("reposts-str"));
        meme.put(MemesContract.Tables.MEME_REPOSTED,jsonMeme.getString("reposted"));
        meme.put(MemesContract.Tables.MEME_REPOSTABLE,jsonMeme.getString("repostable"));

        meme.put(MemesContract.Tables.MEME_LAT,jsonMeme.getString("lat"));
        meme.put(MemesContract.Tables.MEME_LONG,jsonMeme.getString("long"));

        return context.getContentResolver().insert(MemesContract.Tables.MEMES_CONTENT_URI,meme);
    }

    public static void star(final Context context, final Activity a, final Integer loader, final Integer idmeme, final LoaderManager.LoaderCallbacks<?> callbacks, final Fragment f) {
        // stars a meme with idmeme, then updates the loader

        if(!internetAvailable(context)) {
            Toast.makeText(context, context.getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        final SharedPreferences login = getLogin(context);

        String url = context.getString(R.string.api) + "star";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            Boolean success = jsonRes.getBoolean("success");
                            if(success) {
                                ContentValues values = new ContentValues();
                                values.put(MemesContract.Tables.MEME_STARRED,jsonRes.getString("starred"));
                                values.put(MemesContract.Tables.MEME_STARS_NUM,jsonRes.getString("stars-num"));
                                values.put(MemesContract.Tables.MEME_STARS_STR,jsonRes.getString("stars-str"));

                                context.getContentResolver().update(MemesContract.Tables.buildMemeUriWithID(idmeme),values,null,null);

                                f.getLoaderManager().restartLoader(loader, null, callbacks);
                            }
                            else
                                Toast.makeText(context, jsonRes.getString("error"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            System.out.println(response);
                            Toast.makeText(context, context.getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, context.getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("key", login.getString("key",""));
                params.put("id", String.valueOf(idmeme));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(postRequest);
    }

    public static void repost(final Context context, final Activity a, final Integer loader, final Integer idmeme, final LoaderManager.LoaderCallbacks<?> callbacks, final Fragment f) {
        if(!internetAvailable(context)) {
            Toast.makeText(context, context.getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(context.getString(R.string.Repost));
        builder.setMessage(context.getString(R.string.repost_prompt));

        // Set up the input
        final EditText input = new EditText(context);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(context.getString(R.string.Repost), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String caption = input.getText().toString();

                final SharedPreferences login = getLogin(context);

                String url = context.getString(R.string.api) + "repost";

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonRes = new JSONObject(response);
                                    Boolean success = jsonRes.getBoolean("success");
                                    if(success) {
                                        int idmeme = jsonRes.getInt("idmeme");

                                        Log.i("click", "Going to meme " + idmeme);
                                        String fragTitle = "Meme " + idmeme;
                                        FragmentManager fm = ((FragmentActivity) a).getSupportFragmentManager();
                                        Fragment frag = MemeFragment.newInstance(idmeme);
                                        Fragment already = fm.findFragmentByTag(fragTitle);
                                        if (already != null) frag = already;
                                        fm.beginTransaction()
                                                .replace(R.id.container, frag, fragTitle)
                                                .addToBackStack(fragTitle)
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                                .commit();
                                    }
                                    else
                                        Toast.makeText(context, jsonRes.getString("error"), Toast.LENGTH_SHORT).show();
                                }
                                catch (JSONException e) {
                                    System.out.println(response);
                                    Toast.makeText(context, context.getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, context.getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<>();
                        // the POST parameters:
                        params.put("key", login.getString("key",""));
                        params.put("id", String.valueOf(idmeme));
                        params.put("caption", caption);
                        return params;
                    }
                };
                Volley.newRequestQueue(context).add(postRequest);
            }
        });
        builder.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
