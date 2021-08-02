package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.NonNull
import androidx.appcompat.widget.SwitchCompat
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class RATSwitch : SwitchCompat, IRatComponent {
    private var ratEvent: RATEvent? = null
    private var tempCheckStatus: Boolean = false

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        tempCheckStatus = this.isChecked

    }

    override fun prepareEventToSend() {
        if (ratEvent == null)
            ratEvent = RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                label = this.text.toString()
            )
    }

    override fun setCustomRatEvent(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP ->
                /**
                 * Check status
                 */
                checkStatus()
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkStatus(){
        if(this.isChecked != tempCheckStatus){
            //So Switch status changed so send analytics
            tempCheckStatus = this.isChecked
            prepareEventToSend()
            ratEvent?.let {
                DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
            }
        }
    }
}

