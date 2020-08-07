package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
open class Param {
    companion object{
        const val TAG_CREATE_QUEUE = "create_queue"
        const val TAG_UPDATE_QUEUE = "update_queue"
        const val TAG_DELETE_QUEUE = "delete_queue"

        const val TAG_ADD_MEMBER = "add_member"
        const val TAG_UPDATE_MEMBER = "update_member"
        const val TAG_DELETE_MEMBER = "delete_member"

        const val KEY_QUEUE_NAME = "name"
        const val KEY_QUEUE_DESCRIPTION = "description"

        const val KEY_MEMBER_COUNT = "count"
    }
}