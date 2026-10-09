package code_sys.apkopo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import code_sys.apkopo.data.local.dao.CommissionDao
import code_sys.apkopo.data.local.dao.PhotoDao
import code_sys.apkopo.data.local.dao.RemarkDao
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark

@Database(
    entities = [Commission::class, Remark::class, Photo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun commissionDao(): CommissionDao
    abstract fun remarkDao(): RemarkDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apkopo.db"
                ).build().also { instance = it }
            }
    }
}
