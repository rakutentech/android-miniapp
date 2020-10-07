package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.MiniAppCoroutines
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.profile_settings_activity.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ProfileSettingsActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var profileUrl: String
    private lateinit var profileUrlBase64: String
    private val coroutines = MiniAppCoroutines()
    private val encodePhotoUrlJob: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        profileUrl = settings.profilePictureUrl
        profileUrlBase64 = settings.profilePictureUrlBase64

        showBackIcon()
        setContentView(R.layout.profile_settings_activity)
        renderProfileSettingsScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.settings_menu_save -> {
                updateProfile(editProfileName.text.toString())
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderProfileSettingsScreen() {
        setProfileImage(Uri.parse(settings.profilePictureUrl))
        editProfileName.setText(settings.profileName)
        imageProfile.setOnClickListener { openGallery() }
        textEditPhoto.setOnClickListener { openGallery() }
    }

    private fun updateProfile(name: String) {
        settings.profilePictureUrl = profileUrl
        settings.profilePictureUrlBase64 = profileUrlBase64
        settings.profileName = name.trimEnd()
        finish()
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            val imageUri = data?.data
            setProfileImage(imageUri)
            profileUrl = imageUri.toString()
            encodeImageForMiniApp(profileUrl)
        }
    }

    private fun encodeImageForMiniApp(profileUrl: String) {
        coroutines.buildMainScope(encodePhotoUrlJob).launch {
            try {
                withContext(IO) {
                    val uri = Uri.parse(profileUrl)
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.use {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
                        profileUrlBase64 = BASE_64_DATA_PREFIX + Base64.encodeToString(
                            bytes,
                            Base64.DEFAULT
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setProfileImage(uri: Uri?) {
        Glide.with(this@ProfileSettingsActivity)
            .load(uri).apply(RequestOptions().circleCrop())
            .placeholder(R.drawable.r_logo)
            .into(imageProfile as ImageView)
    }

    companion object {
        private const val PICK_IMAGE = 1001
        private const val BASE_64_DATA_PREFIX = "data:image/png;base64,"

        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, ProfileSettingsActivity::class.java))
        }
    }
}
