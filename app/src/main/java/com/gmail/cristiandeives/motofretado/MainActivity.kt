package com.gmail.cristiandeives.motofretado

import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.ncapdevi.fragnav.FragNavController
import kotlinx.android.synthetic.main.activity_main.*

@MainThread
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var mFragNavController: FragNavController

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_bar_track_bus -> mFragNavController.switchTab(FragNavController.TAB1)
                R.id.bottom_bar_view_map -> mFragNavController.switchTab(FragNavController.TAB2)
                else -> Log.wtf(TAG, "unexpected menu item click: $item")
            }
            true
        }

        val rootFragments = listOf<Fragment>(TrackBusFragment(), ViewMapFragment())
        mFragNavController = FragNavController(
                savedInstanceState,
                supportFragmentManager,
                R.id.fragment_view_group,
                rootFragments,
                FragNavController.TAB1
        )

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.v(TAG, "> onCreateOptionsMenu(menu=$menu)")

        menuInflater.inflate(R.menu.activity_main, menu)

        Log.v(TAG, "< onCreateOptionsMenu(menu=$menu): true")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=$item)")

        val isMenuItemProcessed = if (item.itemId == R.id.action_feedback) {
            val intent = ShareCompat.IntentBuilder.from(this)
                    .setType("message/rfc822")
                    .addEmailTo("cdeives@motorola.com")
                    .setSubject("Moto Fretado feedback")
                    .intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                toast(getString(R.string.no_email_app_available))
            }
            true
        } else {
            Log.wtf(TAG, "unexpected menu item click: $item")
            super.onOptionsItemSelected(item)
        }

        Log.v(TAG, "< onOptionsItemSelected(item=$item): $isMenuItemProcessed")
        return isMenuItemProcessed
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.v(TAG, "> onSaveInstanceState(outState=$outState)")
        super.onSaveInstanceState(outState)

        mFragNavController.onSaveInstanceState(outState)

        Log.v(TAG, "< onSaveInstanceState(outState=$outState)")
    }
}