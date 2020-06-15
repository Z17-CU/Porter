package cu.uci.porter.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.ClientInQueue
import cu.uci.porter.repository.entitys.Queue


@Database(
    entities = [(Client::class), (Queue::class), (ClientInQueue::class)],
    version = 3
)
//@TypeConverters(Converter::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun dao(): Dao

    companion object : SingletonHolder<AppDataBase, Context>({
        Room.databaseBuilder(it, AppDataBase::class.java, "DataBase.db")
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN selected INTEGER NOT NULL DEFAULT 0"
                    )
                    database.execSQL(
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN searched INTEGER NOT NULL DEFAULT 0"
                    )
                    database.execSQL(
                        "ALTER TABLE ${Queue.TABLE_NAME} ADD COLUMN description TEXT NOT NULL DEFAULT ''"
                    )
                }
            })
            .build()
    })
}
