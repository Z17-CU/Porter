package cu.uci.porter.interfaces

import android.view.View
import cu.uci.porter.repository.entitys.Queue


interface onClickListener {
    fun onClick(queue: Queue)
    fun onLongClick(view: View, queue: Queue)
}