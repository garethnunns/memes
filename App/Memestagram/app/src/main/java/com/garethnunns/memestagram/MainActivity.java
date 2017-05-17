package com.garethnunns.memestagram;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Memestagram", "Welcome");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!memestagram.loggedIn(getApplicationContext()))
            memestagram.logout(getApplicationContext(),this);

        Fragment feed = new FeedFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container,feed);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return false;

            case R.id.action_starred:
                Toast.makeText(this, "Starred selected", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_logout:
                memestagram.logout(getApplicationContext(),this);
                break;

            default:
                break;
        }

        return true;
    }
}
