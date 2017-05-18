package com.garethnunns.memestagram;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private int selectedItem;

    private static final String ARG_SELECTED = "arg_selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Memestagram", "Welcome");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!memestagram.loggedIn(getApplicationContext()))
            memestagram.logout(getApplicationContext(),this);

        bottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem goToItem;
        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt(ARG_SELECTED, 0);
            goToItem = bottomNav.getMenu().findItem(selectedItem);
        }
        else
            goToItem = bottomNav.getMenu().getItem(0);
        selectFragment(goToItem);
    }

    private void selectFragment(MenuItem item) {
        if(selectedItem == item.getItemId())
            return; // don't do anything if they're already on that page
        // update selected item
        selectedItem = item.getItemId();

        Fragment frag = null;

        String fragTitle = "Bottom "+item.getItemId();

        FragmentManager fm = getSupportFragmentManager();
        Fragment already= fm.findFragmentByTag(fragTitle);
        if(already != null) {
            Log.i("selectFragment()",fragTitle+" is already in stack");
            frag = already;
        }
        else {
            // init corresponding fragment
            switch (selectedItem) {
                case R.id.bottom_feed: {
                    frag = FeedFragment.newInstance(FeedFragment.FEED);
                    break;
                }
                case R.id.bottom_hot:
                    frag = FeedFragment.newInstance(FeedFragment.HOT);
                    break;
                case R.id.bottom_add:
                    //frag = MenuFragment.newInstance(getString(R.string.text_search), getColorFromRes(R.color.color_search));
                    break;
                case R.id.bottom_notifications:
                    //frag = MenuFragment.newInstance(getString(R.string.text_search), getColorFromRes(R.color.color_search));
                    break;
                case R.id.bottom_profile:
                    //frag = MenuFragment.newInstance(getString(R.string.text_search), getColorFromRes(R.color.color_search));
                    break;
                case R.id.action_starred:
                    frag = StarredFragment.newInstance();
                    break;
            }
        }

        if (frag != null) {
            fm.beginTransaction()
                    .replace(R.id.container, frag, fragTitle)
                    .addToBackStack(fragTitle)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_SELECTED, selectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        int next = fm.getBackStackEntryCount()-2;
        String nextName = fm.getBackStackEntryAt(next).getName();

        if(selectedItem == R.id.bottom_feed) {
            for(int i = fm.getBackStackEntryCount(); i >= 0 ; i--)
                fm.popBackStack(); // clear backstack
            super.onBackPressed();
        }
        else {
            for (int i = 0; i< bottomNav.getMenu().size(); i++) {
                MenuItem menuItem = bottomNav.getMenu().getItem(i);
                if (nextName.equals("Bottom " + menuItem.getItemId())) {
                    menuItem.setChecked(true);
                    selectedItem = menuItem.getItemId();
                }
            }

            fm.popBackStack();
        }
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
                selectFragment(item);
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
