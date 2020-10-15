package cu.control.queue.utils

import android.content.Context

object DensityUtil {
    /**
     * dip to px
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px to dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * sp to px
     */
    fun px2sp(context: Context, spValue: Float): Float {
        return spValue * context.resources.displayMetrics.scaledDensity
    }
}