package com.yury.recyclerview.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    // База с Типовыми нарушениями и ссылками на Норм.Акты
    Item::class,
    // База Месторождений и отдельных цехов
    DivisionDB::class,
    // Кустовые площадки на месторождении
    ObjectDB::class,
    // ОПО на Кустовых площадках (Скважины, АГЗУ, Площадки и т.д)
    HazardousProdFacility::class,
    // Статистика. Общий свод данных
    StatisticDB::class,
    // АПК ОПО — комиссии, замечания и фото
    Commission::class,
    Remark::class,
    Photo::class],
    version = 4)
abstract class MainDB : RoomDatabase() {
    abstract fun getDao(): Dao

    // АПК ОПО
    abstract fun getCommissionDao(): CommissionDao
    abstract fun getRemarkDao(): RemarkDao
    abstract fun getPhotoDao(): PhotoDao

    companion object {
        fun getDB(context: Context): MainDB {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDB::class.java,
                "Main_4.db"
            ).build()
        }
    }
}