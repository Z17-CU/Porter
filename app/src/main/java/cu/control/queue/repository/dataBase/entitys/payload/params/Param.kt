package cu.control.queue.repository.dataBase.entitys.payload.params

import androidx.annotation.Keep

@Keep
open class Param {

    companion object{
        const val TAG_CREATE_QUEUE = "01_create_queue"
        const val TAG_UPDATE_QUEUE = "02_update_queue"
        const val TAG_FINISH_QUEUE = "06_finish_queue"
        const val TAG_DELETE_QUEUE = "07_delete_queue"
        const val TAG_CLOSE_QUEUE = "08_close_queue"

        const val TAG_ADD_MEMBER = "03_add_member"
        const val TAG_UPDATE_MEMBER = "04_update_member"
        const val TAG_DELETE_MEMBER = "05_delete_member"


        const val KEY_QUEUE_NAME = "name"
        const val KEY_QUEUE_DESCRIPTION = "description"
        const val KEY_QUEUE_PRODUCTS = "products"
        const val KEY_QUEUE_ALERT = "alertable"
        const val KEY_STORE_VERSION = "store_version"

        const val KEY_MEMBER_COUNT = "count"
    }
}