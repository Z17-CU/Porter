package cu.control.queue.interfaces

import cu.control.queue.repository.entitys.Client


interface onSave {
    fun save(client: Client)
}