package com.garethnunns.memestagram;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import static com.garethnunns.memestagram.memestagram.getLogin;
import static com.garethnunns.memestagram.memestagram.loggedIn;
import static com.garethnunns.memestagram.memestagram.logout;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private int selectedItem;

    private static final String ARG_SELECTED = "arg_selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Memestagram", "Welcome");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!loggedIn(getApplicationContext()))
            logout(getApplicationContext(),this);

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
        FragmentManager fm = getSupportFragmentManager();
        String fragTitle = "Bottom "+item.getItemId();

        if(fm.getBackStackEntryCount()>1 && fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName().equals("Bottom "+selectedItem))
            return;// don't do anything if they're already on that page

        // update selected item
        selectedItem = item.getItemId();

        Fragment frag = null;

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
                    SharedPreferences login = getLogin(getApplicationContext());
                    Long id = login.getLong("iduser",-1);
                    if(id == -1) logout(getApplicationContext(),this);
                    String username = login.getString("username","username");
                    frag = ProfileFragment.newInstance(id, username);
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

        if(fm.getBackStackEntryCount() > 1) {
            for (int i = 0; i< bottomNav.getMenu().size(); i++) {
                MenuItem menuItem = bottomNav.getMenu().getItem(i);
                if (fm.getBackStackEntryAt(fm.getBackStackEntryCount()-2).getName().equals("Bottom " + menuItem.getItemId())) {
                    menuItem.setChecked(true);
                    selectedItem = menuItem.getItemId();
                }
            }
            fm.popBackStack();
        }
        else {
            for(int i = fm.getBackStackEntryCount(); i >= 0 ; i--)
                fm.popBackStack(); // clear backstack
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
