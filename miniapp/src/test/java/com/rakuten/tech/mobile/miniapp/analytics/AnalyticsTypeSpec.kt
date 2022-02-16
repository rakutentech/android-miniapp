package com.rakuten.tech.mobile.miniapp.analytics

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class AnalyticsTypeSpec {

    @Test
    fun `Etypes values should match with the required values`() {
        Etype.APPEAR.value shouldBeEqualTo "appear"
        Etype.CLICK.value shouldBeEqualTo "click"
    }

    @Test
    fun `Actype values should match with the required values`() {
        Actype.HOST_LAUNCH.value shouldBeEqualTo "mini_app_host_launch"
        Actype.OPEN.value shouldBeEqualTo "mini_app_open"
        Actype.CLOSE.value shouldBeEqualTo "mini_app_close"
        Actype.SIGNATURE_VALIDATION_SUCCESS.value shouldBeEqualTo "mini_app_signature_validation_success"
        Actype.SIGNATURE_VALIDATION_FAIL.value shouldBeEqualTo "mini_app_signature_validation_fail"
        Actype.GET_UNIQUE_ID.value shouldBeEqualTo "mini_app_get_unique_id"
        Actype.REQUEST_PERMISSION.value shouldBeEqualTo "mini_app_request_permission"
        Actype.REQUEST_CUSTOM_PERMISSIONS.value shouldBeEqualTo "mini_app_request_custom_permissions"
        Actype.SHARE_INFO.value shouldBeEqualTo "mini_app_share_info"
        Actype.LOAD_AD.value shouldBeEqualTo "mini_app_load_ad"
        Actype.SHOW_AD.value shouldBeEqualTo "mini_app_show_ad"
        Actype.GET_USER_NAME.value shouldBeEqualTo "mini_app_get_user_name"
        Actype.GET_PROFILE_PHOTO.value shouldBeEqualTo "mini_app_get_profile_photo"
        Actype.GET_ACCESS_TOKEN.value shouldBeEqualTo "mini_app_get_access_token"
        Actype.GET_POINTS.value shouldBeEqualTo "mini_app_get_points"
        Actype.SET_SCREEN_ORIENTATION.value shouldBeEqualTo "mini_app_set_screen_orientation"
        Actype.GET_CONTACTS.value shouldBeEqualTo "mini_app_get_contacts"
        Actype.SEND_MESSAGE_TO_CONTACT.value shouldBeEqualTo "mini_app_send_message_to_contact"
        Actype.SEND_MESSAGE_TO_CONTACT_ID.value shouldBeEqualTo "mini_app_send_message_to_contact_id"
        Actype.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.value shouldBeEqualTo "mini_app_send_message_to_multiple_contacts"
        Actype.GET_HOST_ENVIRONMENT_INFO.value shouldBeEqualTo "mini_app_get_host_environment_info"
    }
}
