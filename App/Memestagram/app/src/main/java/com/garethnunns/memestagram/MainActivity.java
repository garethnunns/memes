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

        /*Fragment feed = new FeedFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container,feed);
        ft.commit();*/

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
        // update selected item
        selectedItem = item.getItemId();

        Fragment frag = null;

        String fragTitle = "Fragment "+item.getItemId();

        FragmentManager fm = getSupportFragmentManager();
        Fragment already= fm.findFragmentByTag(fragTitle);
        if(already != null) {
            Log.i("selectFragment()",fragTitle+" is already in stack");
            fm.beginTransaction().remove(already).commit();
            frag = already;
            //fm.popBackStack(fragTitle, 0);
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
            }
        }

        if (frag != null) {
            fm.beginTransaction()
                    .add(R.id.container, frag, fragTitle)
                    .addToBackStack(fragTitle)
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
        // TODO: fix back button so the bottom navigation changes and it doesn't have a blank at the end
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
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
