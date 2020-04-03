package cu.uci.porter.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cu.uci.porter.repository.entitys.Client

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClient(client: Client)

    @Query("SELECT * FROM ${Client.TABLE_NAME} ORDER BY lastRegistry")
    fun getAllClients(): LiveData<List<Client>>

    @Query("SELECT COUNT(*) from ${Client.TABLE_NAME} WHERE id = :id")
    fun clientExist(id: Long) : Int
}