package com.motorola.cdeives.motofretado;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    @MainThread
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "> onCreate(" + savedInstanceState + ")");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(TAG, "< onCreate(" + savedInstanceState + ")");
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
}
