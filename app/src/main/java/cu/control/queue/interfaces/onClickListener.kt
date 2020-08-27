package cu.control.queue.interfaces

import android.view.View
import cu.control.queue.repository.dataBase.entitys.Queue


interface onClickListener {
    fun onClick(queue: Queue)
    fun onLongClick(view: View, queue: Queue)
    fun onDownloadClick(queue: Queue, openQueue: Boolean = false)
    fun onSaveClick(queue: Queue, delete: Boolean)
}