package cu.control.queue.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R
import cu.control.queue.utils.DensityUtil


class SwipeToDeleteCallback(private val mAdapter: AdapterClient, context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var mIconCheck: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_check)!!
    private var mIconUnCheck: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_uncheck)!!
    private var mIconDelete: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
    private var backgroundCheck: ColorDrawable =
        ColorDrawable(ContextCompat.getColor(context, R.color.google_green))
    private var backgroundUnCheck: ColorDrawable =
        ColorDrawable(ContextCompat.getColor(context, R.color.google_red))
    private var backgroundDelete: ColorDrawable =
        ColorDrawable(ContextCompat.getColor(context, R.color.gray_deleter))

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        mAdapter.swipeItem(position, direction)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        val isCheck =
            (recyclerView.adapter as AdapterClient).contentList[viewHolder.adapterPosition].isChecked

        when {
            dX > 0 -> { // Swiping to the right
                backgroundDelete.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + backgroundCornerOffset,
                    itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to the left
                if (isCheck) {
                    backgroundUnCheck.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                } else {
                    backgroundCheck.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                }
            }
            else -> { // view is unSwiped
                backgroundDelete.setBounds(0, 0, 0, 0)
                backgroundCheck.setBounds(0, 0, 0, 0)
                backgroundUnCheck.setBounds(0, 0, 0, 0)
            }
        }
        backgroundDelete.draw(c)
        backgroundCheck.draw(c)
        backgroundUnCheck.draw(c)

        val paint = Paint()

        paint.color = Color.WHITE
        paint.textSize = DensityUtil.px2sp(viewHolder.itemView.context, 10F)

        when {
            dX > 0 -> { // Swiping to the right

                val iconMargin = (itemView.height - mIconDelete.intrinsicHeight) / 2
                val iconTop =
                    itemView.top + (itemView.height - mIconDelete.intrinsicHeight) / 3
                val iconBottom = iconTop + mIconDelete.intrinsicHeight

                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + mIconDelete.intrinsicWidth
                mIconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                backgroundDelete.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset,
                    itemView.bottom
                )
                backgroundDelete.draw(c)
                mIconDelete.draw(c)

                c.drawText(
                    recyclerView.context.getText(R.string.delete).toString(),
                    (itemView.left + (iconMargin / 1.2)).toFloat(),
                    (itemView.bottom - (iconMargin / 2)).toFloat(),
                    paint
                )
            }
            dX < 0 -> { // Swiping to the left

                if (isCheck) {

                    val iconMargin = (itemView.height - mIconUnCheck.intrinsicHeight) / 2
                    val iconTop =
                        itemView.top + (itemView.height - mIconUnCheck.intrinsicHeight) / 3
                    val iconBottom = iconTop + mIconUnCheck.intrinsicHeight

                    val iconLeft = itemView.right - iconMargin - mIconUnCheck.intrinsicWidth - 15
                    val iconRight = itemView.right - iconMargin - 15
                    mIconUnCheck.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    backgroundUnCheck.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                    backgroundUnCheck.draw(c)
                    mIconUnCheck.draw(c)

                    c.drawText(
                        recyclerView.context.getText(R.string.deschequiar).toString(),
                        (itemView.right - (iconMargin * 4)).toFloat(),
                        (itemView.bottom - (iconMargin / 2)).toFloat(),
                        paint
                    )

                } else {

                    val iconMargin = (itemView.height - mIconCheck.intrinsicHeight) / 2
                    val iconTop =
                        itemView.top + (itemView.height - mIconCheck.intrinsicHeight) / 3
                    val iconBottom = iconTop + mIconCheck.intrinsicHeight

                    val iconLeft = itemView.right - iconMargin - mIconCheck.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    mIconCheck.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    backgroundCheck.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                    backgroundCheck.draw(c)
                    mIconCheck.draw(c)

                    c.drawText(
                        recyclerView.context.getText(R.string.chequiar).toString(),
                        (itemView.right - (iconMargin * 2.8)).toFloat(),
                        (itemView.bottom - (iconMargin / 2)).toFloat(),
                        paint
                    )
                }
            }
            else -> { // view is unSwiped
                backgroundCheck.setBounds(0, 0, 0, 0)
                backgroundUnCheck.setBounds(0, 0, 0, 0)
                backgroundDelete.setBounds(0, 0, 0, 0)
            }
        }

    }
}