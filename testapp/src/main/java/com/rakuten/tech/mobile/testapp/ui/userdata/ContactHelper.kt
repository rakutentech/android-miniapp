
package com.rakuten.tech.mobile.testapp.ui.userdata

import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import java.security.SecureRandom
import java.util.*

object ContactHelper {
    val fakeFirstNames = arrayOf(
        "Yvonne",
        "Jamie",
        "Leticia",
        "Priscilla",
        "Sidney",
        "Nancy",
        "Edmund",
        "Bill",
        "Megan"
    )
    val fakeLastNames = arrayOf(
        "Andrews",
        "Casey",
        "Gross",
        "Lane",
        "Thomas",
        "Patrick",
        "Strickland",
        "Nicolas",
        "Freeman"
    )
    private val fakeEmails = listOf(
        "andrews@sample.com",
        "casey@sample.com",
        "gross@sample.com",
        "lane@sample.com",
        "thomas@sample.com",
        "patrick@sample.com",
        "strickland@sample.com",
        "nicolas@sample.com",
        "freeman@sample.com"
    )


    @Suppress("UnusedPrivateMember", "MagicNumber")
    fun createRandomContactList(): ArrayList<Contact> = ArrayList<Contact>().apply {
        for (i in 1..10) {
            this.add(createRandomContact())
        }
    }

    @Suppress("MaxLineLength")
    fun createRandomContact(): Contact {
        val firstName = fakeFirstNames[(SecureRandom().nextDouble() * fakeFirstNames.size).toInt()]
        val lastName = fakeLastNames[(SecureRandom().nextDouble() * fakeLastNames.size).toInt()]
        val email =
            firstName.lowercase(Locale.ROOT) + "." + lastName.lowercase(Locale.ROOT) + "@example.com"
        return Contact(UUID.randomUUID().toString().trimEnd(), "$firstName $lastName", email, fakeEmails)
    }
}
