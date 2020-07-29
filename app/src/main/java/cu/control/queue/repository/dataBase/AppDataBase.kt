package cu.control.queue.repository.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.ClientInQueue
import cu.control.queue.repository.dataBase.entitys.Queue


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
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN selected INTEGER"
                    )
                    database.execSQL(
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN searched INTEGER"
                    )
                    database.execSQL(
                        "ALTER TABLE ${Queue.TABLE_NAME} ADD COLUMN description TEXT NOT NULL DEFAULT ''"
                    )
                }
            })
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN onBlackList INTEGER"
                    )
                    database.execSQL(
                        "ALTER TABLE ${Client.TABLE_NAME} ADD COLUMN repeatedClient INTEGER"
                    )
                    database.execSQL(
                        "ALTER TABLE ${ClientInQueue.TABLE_NAME} ADD COLUMN repeatedClient INTEGER"
                    )
                }
            })
            .build()
    })
}
