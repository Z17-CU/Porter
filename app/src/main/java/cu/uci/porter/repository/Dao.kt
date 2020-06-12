package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.ClientInQueue
import cu.uci.porter.repository.entitys.Queue

@Dao
interface Dao {

    @Insert(onConflict = REPLACE)
    fun insertClient(client: Client)

    @Insert(onConflict = REPLACE)
    fun insertClient(clients: List<Client>)

    @Insert(onConflict = REPLACE)
    fun insertQueue(queue: Queue)

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id = :id")
    fun getClient(id: Long): Client

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id IN (:idList)")
    fun getClients(idList: List<Long>): List<Client>

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id IN (:idList) AND age BETWEEN :min AND :max")
    fun getClientsInRange(idList: List<Long>, min: Int, max: Int): List<Client>

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE id = :id")
    fun getQueue(id: Long): Queue

    @Delete
    fun deleteQueue(queue: Queue)

    @Query("SELECT * FROM ${Queue.TABLE_NAME}")
    fun getAllQueues(): LiveData<List<Queue>>

    @Query("SELECT COUNT(*) from ${ClientInQueue.TABLE_NAME} WHERE queueId = :id")
    fun clientsByQueue(id: Long): Int

    @Query("SELECT * from ${ClientInQueue.TABLE_NAME} WHERE clientId = :idClient AND queueId = :idQueue")
    fun getClientFromQueue(idClient: Long, idQueue: Long): ClientInQueue?

    @Insert(onConflict = REPLACE)
    fun insertClientInQueue(clientInQueue: ClientInQueue)

    @Insert(onConflict = REPLACE)
    fun insertClientInQueue(clientInQueue: List<ClientInQueue>)

    @Query("SELECT * FROM ${ClientInQueue.TABLE_NAME} WHERE queueId = :queueId")
    fun getClientsInQueue(queueId: Long): LiveData<List<ClientInQueue>>

    @Query("SELECT * FROM ${ClientInQueue.TABLE_NAME} WHERE queueId = :queueId")
    fun getClientsInQueueList(queueId: Long): List<ClientInQueue>

    @Query("SELECT * FROM ${ClientInQueue.TABLE_NAME}")
    fun getClientsInQueue(): LiveData<List<ClientInQueue>>

    @Query("SELECT queueId FROM ${ClientInQueue.TABLE_NAME} WHERE clientId = :clientId GROUP BY queueId")
    fun getQueuesIdsByClient(clientId: Long): List<Long>?

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE id IN (:ids)")
    fun getQueuesByIds(ids: List<Long>): List<Queue>?
}