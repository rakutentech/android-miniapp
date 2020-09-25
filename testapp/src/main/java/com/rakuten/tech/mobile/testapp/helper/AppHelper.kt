package com.rakuten.tech.mobile.testapp.helper

import androidx.appcompat.widget.AppCompatEditText

fun isInputEmpty(input: AppCompatEditText): Boolean {
    return input.text.toString().isEmpty() || input.text.toString().isBlank()
}
