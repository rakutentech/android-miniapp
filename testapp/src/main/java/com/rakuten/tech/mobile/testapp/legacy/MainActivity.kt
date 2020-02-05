package com.rakuten.tech.mobile.testapp.legacy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.legacy.platform.MiniAppPlatformImpl
import com.rakuten.tech.mobile.testapp.legacy.main.SectionsPagerAdapter
import timber.log.Timber

/**
 * Test app's main activity.
 */
class MainActivity : AppCompatActivity() {

    /**
     * OnCreate callback from Android system.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                this,
                supportFragmentManager
            )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, MiniAppActivity::class.java))
        }
    }

    /**
     * OnResume callback from Android system.
     */
    override fun onResume() {
        super.onResume()
        MiniAppPlatformImpl().download("miniapp/$APP_ID/version/$VERSION_ID/manifest/")

        Timber.tag("Mini_").d("Start downloading miniapp")
    }

    companion object {
        const val APP_ID = "d005410f-4055-4d95-a0c9-899a60c5936a"
        const val VERSION_ID = "a4e9e618-bda6-4d46-bd62-503ff5e32864"
    }
}
