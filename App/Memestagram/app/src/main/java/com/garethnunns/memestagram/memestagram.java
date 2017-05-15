package com.garethnunns.memestagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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

    public static void logout(Context context) {
        // logs the user out
        SharedPreferences login = getLogin(context);
        login.edit().clear().commit();
        Intent gologin = new Intent(context,LoginActivity.class);
        context.startActivity(gologin);
        ((Activity) context).finish();
    }
}
