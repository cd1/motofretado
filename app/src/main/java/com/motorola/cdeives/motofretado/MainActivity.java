package com.motorola.cdeives.motofretado;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ncapdevi.fragnav.FragNavController;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private FragNavController mFragNavController;

    @Override
    @MainThread
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate(savedInstanceState=" + savedInstanceState + ")");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView =
                (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        List<Fragment> rootFragments = Arrays.asList(new TrackBusFragment(), new ViewMapFragment());
        mFragNavController = new FragNavController(savedInstanceState, getSupportFragmentManager(),
                R.id.fragment_view_group, rootFragments, FragNavController.TAB1);

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
                mFragNavController.switchTab(FragNavController.TAB1);
                break;
            case R.id.bottom_bar_view_map:
                mFragNavController.switchTab(FragNavController.TAB2);
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
        mFragNavController.onSaveInstanceState(outState);

        Log.v(TAG, "< onSaveInstanceState(outState=" + outState+ ")");
    }
}
