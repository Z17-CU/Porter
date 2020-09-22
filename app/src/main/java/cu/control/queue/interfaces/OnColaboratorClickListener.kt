package cu.control.queue.interfaces

 import android.view.View
 import cu.control.queue.repository.dataBase.entitys.payload.Person

interface OnColaboratorClickListener {
    fun onClick(colaborator: Person)
    fun onLongClick(view: View, colaborator: Person)
    fun onSwipe(direction: Int, colaborator: Person)
}