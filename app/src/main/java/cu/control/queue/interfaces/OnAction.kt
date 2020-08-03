package cu.control.queue.interfaces

import android.content.Context
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import cu.control.queue.repository.dataBase.entitys.payload.params.Param

interface OnAction {
    fun onRegistreAction(queueId: String, param: Param, paramTag: String, context: Context)
}