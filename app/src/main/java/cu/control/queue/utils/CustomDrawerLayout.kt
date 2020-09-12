package cu.control.queue.utils

import android.content.Context
import android.util.AttributeSet
import androidx.drawerlayout.widget.DrawerLayout

class CustomDrawerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DrawerLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY)
        var newHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}