package cu.control.queue.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cu.control.queue.R

class SwipeToDeleteCallback(private val mAdapter: AdapterClient, context: Context): ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private var mIconLeft: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_check_list_white)!!
        private var mIconRight: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_uncheck)!!
        private var backgroundLeft: ColorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.google_green))
        private var backgroundRight: ColorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.google_red))

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            mAdapter.deleteItem(position, direction)
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

            when {
                dX > 0 -> { // Swiping to the right
                    backgroundLeft.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + backgroundCornerOffset,
                        itemView.bottom
                    )
                }
                dX < 0 -> { // Swiping to the left
                    backgroundRight.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                }
                else -> { // view is unSwiped
                    backgroundLeft.setBounds(0, 0, 0, 0)
                    backgroundRight.setBounds(0, 0, 0, 0)
                }
            }
            backgroundLeft.draw(c)
            backgroundRight.draw(c)

            val iconMargin = (itemView.height - mIconLeft.intrinsicHeight) / 2
            val iconTop =
                itemView.top + (itemView.height - mIconLeft.intrinsicHeight) / 2
            val iconBottom = iconTop + mIconLeft.intrinsicHeight

            when {
                dX > 0 -> { // Swiping to the right
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + mIconLeft.intrinsicWidth
                    mIconLeft.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    backgroundLeft.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                    backgroundLeft.draw(c)

                    mIconLeft.draw(c)
                }
                dX < 0 -> { // Swiping to the left
                    val iconLeft = itemView.right - iconMargin - mIconRight.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    mIconRight.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    backgroundRight.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                    backgroundRight.draw(c)
                    mIconRight.draw(c)
                }
                else -> { // view is unSwiped
                    backgroundLeft.setBounds(0, 0, 0, 0)
                    backgroundRight.setBounds(0, 0, 0, 0)
                }
            }

        }
    }