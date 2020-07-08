package cu.control.queue.interfaces

import android.view.View
import cu.control.queue.repository.entitys.Client

interface OnClientClickListener {
    fun onClick(client: Client)
    fun onLongClick(view: View, client: Client)
}