package com.rakuten.tech.mobile.testapp.ui.userdata

import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact

interface ContactAdapterPresenter {
    fun addContact(position: Int, contact: Contact)
    fun addContactList(contacts: ArrayList<Contact>)
    fun removeContactAt(position: Int)
    fun provideContactEntries(): ArrayList<Contact>
}
