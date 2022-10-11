package com.rakuten.tech.mobile.testapp.ui.miniapptabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppMainLayoutBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.extensions.setupWithNavController
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments.MiniAppDisplayFragment
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.mini_app_main_layout.*


class DemoAppMainActivity : BaseActivity() {
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var binding: MiniAppMainLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_main_layout)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            setUpBottomNavigationBar()
        }
        launchMiniApps()
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
            Log.d("TAG", "controller: $controller, destination: $destination, arguments: $arguments")
            Log.d("TAG","controller graph: ${controller.graph.id}")

            //Set the mini app settings depending on the tab.
            when (controller.graph.id) {
                R.id.nav_tab_0 -> AppSettings.instance.newMiniAppSdkConfig =
                    AppSettings.instance.miniAppSettings1
                R.id.nav_tab_1 -> AppSettings.instance.newMiniAppSdkConfig =
                    AppSettings.instance.miniAppSettings2
                R.id.nav_tab_2 -> {}
                R.id.nav_tab_3 -> {}

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
                fragment.handlePermissionResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onBackPressed() {
        getCurrentVisibleFragment()?.let { fragment ->
            if (fragment is MiniAppDisplayFragment) {
                fragment.onBackPressed()
            } else super.onBackPressed()

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
