package cu.uci.porter.utils

import android.content.Context
import android.util.AttributeSet
import com.takisoft.preferencex.EditTextPreference
import cu.uci.porter.utils.Conts.Companion.DEFAULT_QUEUE_TIME_HOURS

class IntEditTextPreference : EditTextPreference {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun getPersistedString(defaultReturnValue: String?): String {
        return java.lang.String.valueOf(getPersistedInt(DEFAULT_QUEUE_TIME_HOURS))
    }

    override fun persistString(value: String?): Boolean {
        return persistInt(Integer.valueOf(value!!))
    }
}