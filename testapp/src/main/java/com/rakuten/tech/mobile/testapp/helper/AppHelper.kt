package com.rakuten.tech.mobile.testapp.helper

import androidx.appcompat.widget.AppCompatEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun isInputEmpty(input: AppCompatEditText): Boolean {
    return input.text.toString().isEmpty() || input.text.toString().isBlank()
}

fun parseDateToString(format: String, date: Date): String {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date)
}

fun parseStringToDate(format: String, str: String): Date {
    val format = SimpleDateFormat(format, Locale.getDefault())
    try {
        val date = format.parse(str)
        return date
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date()
}
