package cu.control.queue.repository.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cu.control.queue.repository.dataBase.converters.Converters
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue
import cu.control.queue.repository.dataBase.entitys.payload.Payload
import cu.control.queue.repository.dataBase.entitys.payload.Person


@Database(
    entities = [(Client::class), (Queue::class), (ClientInQueue::class), (Payload::class), (Person::class)],
    version = 8
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun dao(): Dao

    companion object : SingletonHolder<AppDataBase, Context>({

        Room.databaseBuilder(it, AppDataBase::class.java, "DataBase.db")
            .fallbackToDestructiveMigration()
            .build()
    })
}
