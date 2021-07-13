package com.rakuten.tech.mobile.testapp.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class RATButton : AppCompatButton, IRatComponent{

    private lateinit var ratEvent: RATEvent

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        prepareEventForSend()
        //TODO: Send The event
        return returnClick
    }

    override fun prepareEventForSend() {
        ratEvent = RATEvent(
            event = EventType.CLICK,
            action = Actiontype.OPEN
        )
    }

    override fun setCustomRatEvent(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

}
