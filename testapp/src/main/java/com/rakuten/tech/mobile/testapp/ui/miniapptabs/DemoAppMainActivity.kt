package com.rakuten.tech.mobile.testapp.ui.miniapptabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppMainLayoutBinding
import com.rakuten.tech.mobile.miniapp.view.MiniAppView
import com.rakuten.tech.mobile.testapp.helper.clearAllCursorFocus
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.deeplink.INTENT_EXTRA_DEEPLINK
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.extensions.setupWithNavController
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments.FeaturesFragment
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments.MiniAppDisplayFragment
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments.MiniAppListFragment
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments.SettingsFragment
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.mini_app_main_layout.*
import kotlinx.coroutines.launch

val miniAppIdAndViewMap = hashMapOf<Pair<Int, String>, MiniAppView>()
val BUNDLE_MINI_APP_ID = "404e46b4-263d-4768-b2ec-8a423224bead"
val BUNDLE_MINI_APP_VERSION_ID = "4c3365a8-7192-4b2e-a290-101ad5987f2e"

class DemoAppMainActivity : BaseActivity() {
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var binding: MiniAppMainLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //unzip the bundle
        launch {
            MiniApp.instance().unzipBundle(
                fileName = "js-miniapp-sample.zip",
                miniAppId = BUNDLE_MINI_APP_ID,
                versionId = BUNDLE_MINI_APP_VERSION_ID
            )
        }

        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_main_layout)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            setUpBottomNavigationBar()
        }
        launchMiniApps()
    }

    override fun onResume() {
        super.onResume()
        if (intent.hasExtra(INTENT_EXTRA_DEEPLINK) &&
            intent.getBooleanExtra(INTENT_EXTRA_DEEPLINK, false)
        ) {
            changeTabMenu(PAGE_SETTINGS)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        clearAllCursorFocus(event, this@DemoAppMainActivity)
        return super.dispatchTouchEvent(event)
    }

    private fun setUpBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.nav_graph_tab_0,
            R.navigation.nav_graph_tab_1,
            R.navigation.nav_graph_tab_2,
            R.navigation.nav_graph_tab_3
        )

        val controller = binding.bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.activity_main_nav_host_fragment,
            intent = intent
        )


        controller.observe(this) { navController ->
            setupActionBarWithNavController(navController)

            // unregister old onDestinationChangedListener, if it exists
            currentNavController?.value?.removeOnDestinationChangedListener(
                onDestinationChangedListener
            )

            // add onDestinationChangedListener to the new NavController
            navController.addOnDestinationChangedListener(onDestinationChangedListener)
        }

        currentNavController = controller
    }

    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            Log.d(
                "TAG",
                "controller: $controller, destination: $destination, arguments: $arguments"
            )
            Log.d("TAG", "controller graph: ${controller.graph.id}")

            //Set the mini app settings depending on the tab.
            when (controller.graph.id) {
                PAGE_1 -> {
                    AppSettings.instance.setTab1MiniAppSdkConfig()
                }
                PAGE_2 -> {
                    AppSettings.instance.setTab2MiniAppSdkConfig()
                }
                PAGE_FEATURES -> {
                    //do nothing intended
                }
                PAGE_SETTINGS -> {
                    //do nothing intended
                }

            }
            // if you need to show/hide bottom nav or toolbar based on destination
            // binding.bottomNavigationView.isVisible = destination.id != R.id.miniappdisplayFragment
        }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    private fun launchMiniApps() {
        if (AppSettings.instance.isSettingSaved) {
            changeTabMenu(PAGE_1)
        } else {
            changeTabMenu(PAGE_SETTINGS)
        }
    }

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private fun changeTabMenu(page: Int) {
        bottomNavigationView.selectedItemId = page
    }

    fun getCurrentSelectedId() = bottomNavigationView.selectedItemId

    companion object {
        private const val PAGE_1 = R.id.nav_tab_0
        private const val PAGE_2 = R.id.nav_tab_1
        private const val PAGE_FEATURES = R.id.nav_tab_2
        private const val PAGE_SETTINGS = R.id.nav_tab_3
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        getCurrentVisibleFragment()?.let { fragment ->
            if (fragment is MiniAppDisplayFragment) {
                fragment.handleOnActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCurrentVisibleFragment()?.let { fragment ->
            if (fragment is MiniAppDisplayFragment) {
                fragment.handlePermissionResult(requestCode, grantResults)
            }
        }
    }

    override fun onBackPressed() {
        getCurrentVisibleFragment()?.let { fragment ->
            when (fragment) {
                is MiniAppDisplayFragment -> {
                    fragment.onBackPressed()
                }
                is MiniAppListFragment, is FeaturesFragment, is SettingsFragment -> {
                    finish()
                }
                else -> super.onBackPressed()
            }
        }
    }

    private fun getCurrentVisibleFragment(): Fragment? {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.activity_main_nav_host_fragment)
        val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        return if (fragment is Fragment) {
            fragment
        } else null
    }
}
