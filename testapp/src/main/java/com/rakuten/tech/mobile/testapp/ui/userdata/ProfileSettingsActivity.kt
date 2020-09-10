package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.AppScreen
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity
import kotlinx.android.synthetic.main.profile_settings_activity.*
import kotlin.properties.Delegates

class ProfileSettingsActivity : BaseActivity() {

    companion object {
        private const val PICK_IMAGE = 1001

        fun start(activity: Activity) {
            val intent = Intent(activity, ProfileSettingsActivity::class.java)
            intent.putExtra(
                SettingsMenuActivity.SETTINGS_SCREEN_NAME,
                AppScreen.MINI_APP_SETTINGS_ACTIVITY
            )
            activity.startActivity(intent)
        }
    }

    private var saveViewEnabled by Delegates.observable(true) { _, old, new ->
        if (new != old) {
            invalidateOptionsMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.profile_settings_activity)
        initializeActionBar()
        setIcon(null)
        setImageListener()
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        menu.findItem(R.id.settings_menu_save).isEnabled = saveViewEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToPreviousScreen()
                return true
            }
            R.id.settings_menu_save -> {
                onSaveAction()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSaveAction() {
        updateSettings(
            editProfileName.text.toString()
        )
    }

    private fun setImageListener() {
        textAddPhoto.setOnClickListener {
            openGallery()
        }
    }

    private fun updateSettings(name: String) {
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            setIcon(data?.data)
        }
    }

    private fun setIcon(uri: Uri?) {
        Glide.with(this@ProfileSettingsActivity)
            .load(uri).apply(RequestOptions().circleCrop())
            .placeholder(R.drawable.r_logo)
            .into(imageProfile as ImageView)
    }

    private fun navigateToPreviousScreen() {
        when (intent.extras?.getString(SettingsMenuActivity.SETTINGS_SCREEN_NAME)) {
            AppScreen.MINI_APP_SETTINGS_ACTIVITY -> {
                val intent = Intent(this, SettingsMenuActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
            else -> finish()
        }
    }

    override fun onBackPressed() {
        navigateToPreviousScreen()
        super.onBackPressed()
    }
}
