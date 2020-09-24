package com.rakuten.tech.mobile.testapp.ui.userdata

interface ContactAdapterPresenter {
    fun addContact(position: Int, contact: String)
    fun addContactList(contacts: ArrayList<String>)
    fun removeContactAt(position: Int)
    fun provideContactEntries(): ArrayList<String>
}
