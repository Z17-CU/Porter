package cu.control.queue.repository.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.utils.CustomPair

@Dao
interface Dao {

    @Insert(onConflict = REPLACE)
    fun insertPayload(payload: Payload)

    @Query("SELECT * FROM ${Payload.TABLE_NAME} WHERE queue_uuid = :id")
    fun getPayload(id: String): Payload?

    @Delete
    fun deletePayload(payloads: List<Payload>)

    @Query("SELECT * FROM ${Payload.TABLE_NAME}")
    fun getAllPayloadLive(): LiveData<List<Payload>>

    @Insert(onConflict = REPLACE)
    fun insertClient(client: Client)

    @Insert(onConflict = REPLACE)
    fun insertClient(clients: List<Client>)

    @Insert(onConflict = REPLACE)
    fun insertQueue(queue: Queue)

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id = :id")
    fun getClient(id: Long): Client?

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id IN (:idList)")
    fun getClientsInternal(idList: List<Long>): List<Client>

    fun getClients(idList: List<Long>): List<Client> {
        var clientList: List<Client> = ArrayList()

        if (idList.size < 1000) {
            clientList = getClientsInternal(idList)
        } else {
            var a = 0
            var b = 999

            while (a < idList.size) {
                if (b > idList.size) {
                    b = idList.size
                }
                clientList = clientList + getClientsInternal(idList.subList(a, b))
                a = b
                b += 999
            }
        }
        return clientList
    }

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id IN (:idList) AND age BETWEEN :min AND :max")
    fun getClientsInRange(idList: List<Long>, min: Int, max: Int): List<Client>

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE id = :id")
    fun getQueue(id: Long): Queue

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE id = :id")
    fun getQueueLive(id: Long): LiveData<Queue>

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE uuid = :uuid")
    fun getQueueByUUID(uuid: String): Queue?

    @Delete
    fun deleteQueue(queue: Queue)

    @Query("DELETE FROM ${ClientInQueue.TABLE_NAME} WHERE queueId = :id")
    fun deleteAllClientsFromQueue(id: Long)

    @Query("DELETE FROM ${ClientInQueue.TABLE_NAME} WHERE clientId = :clientId AND queueId = :queueId")
    fun deleteClientFromQueue(clientId: Long, queueId: Long)

    @Query("SELECT MAX(number) FROM ${ClientInQueue.TABLE_NAME} WHERE queueId = :queueId")
    fun getLastNumberInQueue(queueId: Long): Int

    @Query("UPDATE ${Queue.TABLE_NAME} SET clientsNumber = :size WHERE id = :queueId")
    fun updateQueueSize(queueId: Long, size: Int)

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

    @Query("SELECT COUNT(*) FROM ${ClientInQueue.TABLE_NAME} WHERE clientId = :id AND (lastRegistry BETWEEN :beginDate AND :endDate)")
    fun countToAlert(id: Long, beginDate: Long, endDate: Long): Int

    @Query("SELECT * FROM ${ClientInQueue.TABLE_NAME} where queueId = :queue1Id or queueId = :queue2Id group by clientId order by number")
    fun getClientInQueueBy2Queues(queue1Id: Long, queue2Id: Long): List<ClientInQueue>

    @Query("SELECT clientId as first, Count(*) as second FROM ${ClientInQueue.TABLE_NAME} where queueId = :queue1Id or queueId = :queue2Id group by first order by number")
    fun getClientRepeatedClients(queue1Id: Long, queue2Id: Long): List<CustomPair>

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE onBlackList = 1")
    fun getClientInBlackList(): LiveData<List<Client>>

    @Query("SELECT * FROM ${Person.TABLE_NAME} WHERE ci IN (:idList)")
    fun getCollaboratorsPrivate(idList: List<String>): List<Person>

    @Insert(onConflict = REPLACE)
    fun insertCollaborator(person: Person)

    @Insert(onConflict = REPLACE)
    fun insertCollaborator(persons: ArrayList<Person>)

    @Query("DELETE FROM ${Person.TABLE_NAME} WHERE ci = :personId")
    fun deleteCollaborator(personId: String)

    @Query("SELECT * FROM ${Person.TABLE_NAME}")
    fun getAllCollaborators(): LiveData<List<Person>>

    @Query("SELECT * FROM ${Person.TABLE_NAME}")
    fun getAllCollaboratorsList(): List<Person>

    fun getCollaborators(idList: List<String>): List<Person> {
        var clientList: List<Person> = ArrayList()

        if (idList.size < 1000) {
            clientList = getCollaboratorsPrivate(idList)
        } else {
            var a = 0
            var b = 999

            while (a < idList.size) {
                if (b > idList.size) {
                    b = idList.size
                }
                clientList = clientList + getCollaboratorsPrivate(idList.subList(a, b))
                a = b
                b += 999
            }
        }
        return clientList
    }
}