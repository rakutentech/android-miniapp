package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import java.io.File

import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact

internal const val TEST_BODY_CONTENT = "lorem ipsum"
internal const val TEST_ERROR_MSG = "error_message"
internal const val TEST_VALUE = "test_value"

internal const val TEST_ID_MINIAPP = "5f0ed952-36ab-43e2-a285-b237c11e23cb"
internal const val TEST_ID_MINIAPP_VERSION = "fa7e1522-adf2-4322-8146-84dca1f812a5"

internal const val TEST_STORAGE_VERSION = 1
internal const val TEST_MAX_STORAGE_SIZE_IN_BYTES = "52428800"

internal const val TEST_BASE_PATH = "dummy"
internal const val TEST_URL_FILE = "file.storage/test/file.abc"
internal const val TEST_URL_HTTPS_1 = "https://www.example.com/1"
internal const val TEST_URL_HTTPS_2 = "https://www.example.com/2/"
internal const val TEST_PHONE_URI = "tel:123456"
internal const val TEST_MAIL_URI = "mailto:test@example.com"
internal const val TEST_URL_PARAMS = "param1=value1&param2=value2"
internal const val TEST_BASE_URL = "https://www.test.com"

internal const val TEST_MA_ID = "test_id"
internal const val TEST_MA_URL = "https://miniapp"
internal const val TEST_MA_DISPLAY_NAME = "test_name"
internal const val TEST_MA_ICON = "test_icon"
internal const val TEST_MA_VERSION_TAG = "test_vtag"
internal const val TEST_MA_VERSION_ID = "test_vid"
internal const val TEST_MA_LANGUAGE_CODE = "default"
internal const val TEST_MA_PREVIEW_CODE = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"

internal const val TEST_HA_NAME = "test_hostapp_name"
internal const val TEST_HA_ID_PROJECT = "test_project_id"
internal const val TEST_HA_SUBSCRIPTION_KEY = "test_subscription_key"

internal const val TEST_CALLBACK_ID = "test_callback_id"
internal const val TEST_CALLBACK_VALUE = "test_callback_value"

internal const val TEST_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
internal val TEST_LIST_PUBLIC_KEY_SSL = emptyList<String>()

private val FILE_SEPARATOR = File.separator
internal val VALID_FILE_URL_PATH =
    "https://www.example.com$FILE_SEPARATOR"
        .plus("map-published-v2${FILE_SEPARATOR}min-872f9172-804f-44e2-addd-ed612170dac9$FILE_SEPARATOR")
        .plus("ver-6181004c-a6aa-4eda-b145-a5ff73fc4ad0${FILE_SEPARATOR}a${FILE_SEPARATOR}b${FILE_SEPARATOR}index.html")
internal const val INVALID_FILE_URL_PATH = "https://78d85043-d04f-486a-8212-bf2601cb63a2/js"

internal const val TEST_USER_NAME = "test_user_name"
internal const val TEST_PROFILE_PHOTO = "data:image/png;base64,encodedValue"
internal val TEST_CONTACT = Contact("test_contact_id", "test_contact_name", "test_contact_email")
internal const val TEST_PROMOTIONAL_URL = "http://testImageurl.co"
internal const val TEST_PROMOTIONAL_TEXT = "test_promotional_text"

internal val TEST_MA = MiniAppInfo(
    id = TEST_MA_ID,
    displayName = TEST_MA_DISPLAY_NAME,
    icon = TEST_MA_ICON,
    version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = TEST_MA_VERSION_ID),
    promotionalImageUrl = TEST_PROMOTIONAL_URL,
    promotionalText = TEST_PROMOTIONAL_TEXT
)

// ACCESS_TOKEN_PERMISSIONS
internal val TEST_ATP1 = AccessTokenScope(
    audience = "aud1", scopes = mutableListOf("scopeA", "scopeB")
)
internal val TEST_ATP2 = AccessTokenScope(
    audience = "aud2", scopes = mutableListOf("scopeB")
)
internal val TEST_ATP_LIST = mutableListOf(TEST_ATP1, TEST_ATP2)

// HOST_APP_ANALYTICS
internal val TEST_CONFIG1 = MiniAppAnalyticsConfig(
    acc = 1, aid = 2
)
internal val TEST_CONFIG2 = MiniAppAnalyticsConfig(
    acc = 3, aid = 4
)
internal val TEST_HA_ANALYTICS_CONFIGS = listOf(TEST_CONFIG1, TEST_CONFIG2)

internal const val TEST_PUBLIC_KEY_ID = "test_public_key_id"
internal const val TEST_MANIFEST_SIGNATURE = "test_manifest_signature"
