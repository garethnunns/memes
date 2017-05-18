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
            fm.beginTransaction().remove(fm.findFragmentByTag(fragTitle)).commitAllowingStateLoss();
            //fm.beginTransaction().remove(already).commit();
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
            }
        }

        if (frag != null) {
            fm.beginTransaction()
                    .add(R.id.container, frag, fragTitle)
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
        // the idea for this is that you go back through the history of pages you've been on

        // it gets messy because we've removed some items out of the stack
        // and android keeps the reference to them
        FragmentManager fm = getSupportFragmentManager();
        int bottom = fm.getBackStackEntryCount()-1;
        int next = bottom-1;
        boolean more = true;
        for(int i = bottom; i >= 0; i--) {
            Fragment f = fm.findFragmentByTag(fm.getBackStackEntryAt(i).getName());
            if (!f.isRemoving() || !f.isDetached()) { // exists
                more = true;
                bottom = i;
            }
            else if((f.isDetached() || f.isRemoving()) && more) // doesn't exist and just after one that does exist
                bottom = i;
            else more = false;
        }
        if (next >= bottom) {
            String name = fm.getBackStackEntryAt(next).getName();
            // select it in the bottom nav if it's there
            for (int i = 0; i< bottomNav.getMenu().size(); i++) {
                MenuItem menuItem = bottomNav.getMenu().getItem(i);
                if(name.equals("Bottom "+menuItem.getItemId()))
                    menuItem.setChecked(true);
            }
            fm.popBackStack(next,0);
        }
        else {
            for(int i = fm.getBackStackEntryCount(); i >= 0 ; i--)
                fm.popBackStack();
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
