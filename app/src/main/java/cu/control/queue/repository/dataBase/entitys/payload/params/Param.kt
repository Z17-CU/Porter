package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
open class Param {
    companion object{
        const val TAG_CREATE_QUEUE = "create_queue"
        const val TAG_UPDATE_QUEUE = "update_queue"
        const val TAG_DELETE_QUEUE = "delete_queue"

        const val KEY_QUEUE_NAME = "name"
        const val KEY_QUEUE_DESCRIPTION = "description"

        const val KEY_MEMBER_COUNT = "count"
    }
}