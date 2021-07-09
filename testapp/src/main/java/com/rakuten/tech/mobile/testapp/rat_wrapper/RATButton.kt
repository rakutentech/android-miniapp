package com.rakuten.tech.mobile.testapp.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import com.rakuten.tech.mobile.miniapp.analytics.RATTrackerDispatcher

class RATButton : AppCompatButton {

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun performClick(): Boolean {
        RATTrackerDispatcher.instance().sendEvent()
        return super.performClick()
    }

}
