package cu.control.queue.interfaces

import cu.control.queue.repository.dataBase.entitys.InterestingClient

interface OnInterestingClickListener {
    fun onClick(client: InterestingClient)
}