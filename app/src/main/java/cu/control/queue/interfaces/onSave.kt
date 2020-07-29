package cu.control.queue.interfaces

import cu.control.queue.repository.dataBase.entitys.Client


interface onSave {
    fun save(client: Client)
}