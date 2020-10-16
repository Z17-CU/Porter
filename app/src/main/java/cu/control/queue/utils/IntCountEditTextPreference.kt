package cu.control.queue.utils

import android.content.Context
import android.util.AttributeSet
import com.takisoft.preferencex.EditTextPreference
import cu.control.queue.utils.Conts.Companion.DEFAULT_QUEUE_COUNT_VERIFY
import cu.control.queue.utils.Conts.Companion.DEFAULT_QUEUE_TIME_HOURS

class IntCountEditTextPreference : EditTextPreference {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun getPersistedString(defaultReturnValue: String?): String {
        return java.lang.String.valueOf(getPersistedInt(DEFAULT_QUEUE_COUNT_VERIFY))
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(Integer.valueOf(value!!))
    }
}