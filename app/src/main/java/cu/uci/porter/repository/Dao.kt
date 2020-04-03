package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClient(client: Client)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQueue(queue: Queue)

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE id = :id")
    fun getClient(id: Long): Client

    @Query("SELECT * FROM ${Queue.TABLE_NAME} WHERE id = :id")
    fun getQueue(id: Int): Queue

    @Query("SELECT * FROM ${Client.TABLE_NAME} WHERE queueId = :id ORDER BY lastRegistry")
    fun getAllClients(id: Int): LiveData<List<Client>>

    @Query("SELECT * FROM ${Queue.TABLE_NAME}")
    fun getAllQueues(): LiveData<List<Queue>>

    @Query("SELECT COUNT(*) from ${Client.TABLE_NAME} WHERE id = :id AND queueId = :queueId")
    fun clientExist(id: Long, queueId: Int): Int

    @Query("SELECT COUNT(*) from ${Client.TABLE_NAME} WHERE queueId = :id")
    fun clientsByQueue(id: Int): Int
}