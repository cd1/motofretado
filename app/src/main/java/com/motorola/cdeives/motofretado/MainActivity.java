package com.motorola.cdeives.motofretado;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

public class MainActivity extends AppCompatActivity
        implements OnMenuTabClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomBar mBottomBar;

    private void setCurrentFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_view_group, fragment);
        tx.commit();
    }

    @Override
    @MainThread
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate(" + savedInstanceState + ")");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItemsFromMenu(R.menu.bottombar, this);

        Log.v(TAG, "< onCreate(" + savedInstanceState + ")");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "> onSaveInstanceState(" + outState + ")");

        super.onSaveInstanceState(outState);
        mBottomBar.onSaveInstanceState(outState);

        Log.v(TAG, "< onSaveInstanceState(" + outState + ")");
    }

    @Override
    @MainThread
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "> onCreateOptionsMenu([Menu])");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        Log.v(TAG, "> onCreateOptionsMenu([Menu])");

        return true;
    }

    @Override
    @UiThread
    public void onMenuTabSelected(@IdRes int menuItemId) {
        Log.v(TAG, "> onMenuTabSelected(" + getResources().getResourceEntryName(menuItemId) + ")");

        switch (menuItemId) {
            case R.id.bottom_bar_track_bus:
                setCurrentFragment(new TrackBusFragment());
                break;
            default:
                Log.wtf(TAG, "I don't know how to handle this tab's click: " +
                        getResources().getResourceEntryName(menuItemId));
        }

        Log.v(TAG, "< onMenuTabSelected(" + getResources().getResourceEntryName(menuItemId) + ")");
    }

    @Override
    @UiThread
    public void onMenuTabReSelected(@IdRes int menuItemId) {
        Log.v(TAG, "onMenuTabReSelected(" + getResources().getResourceEntryName(menuItemId) + ")");
    }
}
