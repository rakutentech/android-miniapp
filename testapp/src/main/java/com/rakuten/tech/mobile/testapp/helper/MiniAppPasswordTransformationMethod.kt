package com.rakuten.tech.mobile.testapp.helper

import android.text.method.PasswordTransformationMethod
import android.view.View

/***
 * This class provides a custom [PasswordTransformationMethod]
 * which converts character sequence to '*' starting from given [maskStartIndex].
 */
class MiniAppPasswordTransformationMethod(private val maskStartIndex: Int = 5) :
    PasswordTransformationMethod() {

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return MaskedCharSequence(source, maskStartIndex)
    }

    inner class MaskedCharSequence(
        private val source: CharSequence,
        private val maskStartIndex: Int
    ) : CharSequence {

        override val length: Int
            get() = source.length

        override fun get(index: Int): Char {
            if (index >= maskStartIndex) return MASKED_CHAR
            return source[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return source.subSequence(startIndex, endIndex)
        }
    }

    private companion object {
        const val MASKED_CHAR = '*'
    }
}
