package cu.uci.porter.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cu.uci.porter.repository.entitys.Client

@Database(
    entities = [(Client::class)],
    version = 1
)
//@TypeConverters(Converter::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun dao(): Dao

    companion object : SingletonHolder<AppDataBase, Context>({
        Room.databaseBuilder(it, AppDataBase::class.java, "DataBase.db")
            .build()
    })
}
