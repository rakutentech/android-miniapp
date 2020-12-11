package com.rakuten.tech.mobile.miniapp

internal const val TEST_BODY_CONTENT = "lorem ipsum"
internal const val TEST_ERROR_MSG = "error_message"
internal const val TEST_VALUE = "test_value"

internal const val TEST_ID_MINIAPP = "5f0ed952-36ab-43e2-a285-b237c11e23cb"
internal const val TEST_ID_MINIAPP_VERSION = "fa7e1522-adf2-4322-8146-84dca1f812a5"

internal const val TEST_BASE_PATH = "dummy"
internal const val TEST_URL_FILE = "file.storage/test/file.abc"
internal const val TEST_URL_HTTPS_1 = "https://www.example.com/1"
internal const val TEST_URL_HTTPS_2 = "https://www.example.com/2/"
internal const val TEST_PHONE_URI = "tel:123456"

internal const val TEST_MA_ID = "test_id"
internal const val TEST_MA_URL = "https://miniapp"
internal const val TEST_MA_DISPLAY_NAME = "test_name"
internal const val TEST_MA_ICON = "test_icon"
internal const val TEST_MA_VERSION_TAG = "test_vtag"
internal const val TEST_MA_VERSION_ID = "test_vid"

internal const val TEST_HA_NAME = "test_hostapp_name"
internal const val TEST_HA_ID_PROJECT = "test_project_id"
internal const val TEST_HA_SUBSCRIPTION_KEY = "test_subscription_key"

internal const val TEST_CALLBACK_ID = "test_callback_id"
internal const val TEST_CALLBACK_VALUE = "test_callback_value"

internal const val TEST_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"

internal const val VALID_FILE_URL_PATH =
    "https://www.example.com/"
        .plus("map-published-v2/min-872f9172-804f-44e2-addd-ed612170dac9/")
        .plus("ver-6181004c-a6aa-4eda-b145-a5ff73fc4ad0/a/b/index.html")
internal const val INVALID_FILE_URL_PATH = "https://78d85043-d04f-486a-8212-bf2601cb63a2/js"

internal const val TEST_USER_NAME = "test_user_name"
internal const val TEST_PROFILE_PHOTO = "data:image/png;base64,encodedValue"
internal const val TEST_CONTACT_ID = "test_contact_id"

internal val TEST_MA = MiniAppInfo(
    id = TEST_MA_ID,
    displayName = TEST_MA_DISPLAY_NAME,
    icon = TEST_MA_ICON,
    version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = TEST_MA_VERSION_ID)
)
