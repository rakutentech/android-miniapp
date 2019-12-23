package com.rakuten.tech.mobile.testapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.legacy.platform.MiniAppPlatformImpl
import com.rakuten.tech.mobile.testapp.main.SectionsPagerAdapter
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
            SectionsPagerAdapter(this, supportFragmentManager)
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
        const val APP_ID = "b9247b09-c534-48c5-bcc5-0644907d22f8"
        const val VERSION_ID = "9761a1dc-a1cb-4730-92ac-e9aa09a87531"
    }
}
