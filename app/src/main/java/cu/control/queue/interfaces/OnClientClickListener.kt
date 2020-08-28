package cu.control.queue.interfaces

import android.text.Layout
import android.view.View
import com.daimajia.swipe.SwipeLayout
import cu.control.queue.repository.dataBase.entitys.Client

interface OnClientClickListener {
    fun onClick(client: Client)
    fun onLongClick(view: View, client: Client)
    fun onSwipe(view: SwipeLayout, client: Client)
}