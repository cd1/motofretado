package com.motorola.cdeives.motofretado;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_NAVIGATION_INDEX = 0;
    private static final String TRACK_BUS_FRAGMENT_TAG = "TrackBus";
    private static final String VIEW_MAP_FRAGMENT_TAG = "ViewMap";
    private static final String BOTTOM_NAVIGATION_INDEX_KEY = "bottomNavigationIndexKey";

    private BottomNavigationView mBottomNavigationView;

    @UiThread
    private void setCurrentFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case TRACK_BUS_FRAGMENT_TAG:
                    fragment = new TrackBusFragment();
                    break;
                case VIEW_MAP_FRAGMENT_TAG:
                    fragment = new ViewMapFragment();
                    break;
                default:
                    Log.wtf(TAG, "unexpected Fragment tag; using default Fragment");
                    fragment = new TrackBusFragment();
            }
        }

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_view_group, fragment, tag);
        tx.commit();
    }

    @Override
    @MainThread
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate(savedInstanceState=" + savedInstanceState + ")");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // only "click" the default tab when creating the activity for the first time
        // (i.e. not when changing configuration)
        if (savedInstanceState == null) {
            ((BottomNavigationMenuView) mBottomNavigationView.getChildAt(0))
                    .getChildAt(DEFAULT_NAVIGATION_INDEX).performClick();
        }

        Log.v(TAG, "< onCreate(savedInstanceState=" + savedInstanceState + ")");
    }

    @Override
    @MainThread
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "> onCreateOptionsMenu([Menu])");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        Log.v(TAG, "< onCreateOptionsMenu([Menu]): true");
        return true;
    }

    @Override
    @MainThread
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "> onOptionsItemSelected(item=" + item + ")");

        boolean isMenuItemProcessed = true;

        switch (item.getItemId()) {
            case R.id.action_feedback:
                Intent intent = ShareCompat.IntentBuilder.from(this)
                        .setType("message/rfc822")
                        .addEmailTo("cdeives@motorola.com")
                        .setSubject("Moto Fretado feedback")
                        .getIntent();
                startActivity(intent);
                break;
            default:
                Log.wtf(TAG, "unexpected menu item click: " + item);
                isMenuItemProcessed = super.onOptionsItemSelected(item);
        }

        Log.v(TAG, "< onOptionsItemSelected(item=" + item + "): " + isMenuItemProcessed);
        return isMenuItemProcessed;
    }

    @Override
    @MainThread
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.v(TAG, "> onNavigationItemSelected(item=" + item + ")");

        switch (item.getItemId()) {
            case R.id.bottom_bar_track_bus:
                setCurrentFragment(TRACK_BUS_FRAGMENT_TAG);
                break;
            case R.id.bottom_bar_view_map:
                setCurrentFragment(VIEW_MAP_FRAGMENT_TAG);
                break;
            default:
                Log.wtf(TAG, "unexpected menu item click: " + item);
        }

        Log.v(TAG, "< onNavigationItemSelected(item=" + item + "): true");
        return true;
    }

    @Override
    @MainThread
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "> onSaveInstanceState(outState=" + outState+ ")");

        super.onSaveInstanceState(outState);

        Menu bottomMenu = mBottomNavigationView.getMenu();
        for (int i = 0; i < bottomMenu.size(); i++) {
            if (bottomMenu.getItem(i).isChecked()) {
                outState.putInt(BOTTOM_NAVIGATION_INDEX_KEY, i);
                break;
            }
        }

        Log.v(TAG, "< onSaveInstanceState(outState=" + outState+ ")");
    }

    @Override
    @MainThread
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG, "> onRestoreInstanceState(savedInstanceState=" + savedInstanceState + ")");

        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            int bottomNavigationIndex = savedInstanceState.getInt(BOTTOM_NAVIGATION_INDEX_KEY);
            ((BottomNavigationMenuView) mBottomNavigationView.getChildAt(0))
                    .getChildAt(bottomNavigationIndex).performClick();
        }

        Log.v(TAG, "< onRestoreInstanceState(savedInstanceState=" + savedInstanceState + ")");
    }
}
