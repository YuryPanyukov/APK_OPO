package com.yury.recyclerview.di

import android.content.Context
import androidx.room.Room
import com.yury.recyclerview.database.CommissionDao
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.database.PhotoDao
import com.yury.recyclerview.database.RemarkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MainDB {
        return Room.databaseBuilder(
            context,
            MainDB::class.java,
            "Main_4.db"
        ).build()
    }

    @Provides
    fun provideCommissionDao(db: MainDB): CommissionDao = db.getCommissionDao()

    @Provides
    fun provideRemarkDao(db: MainDB): RemarkDao = db.getRemarkDao()

    @Provides
    fun providePhotoDao(db: MainDB): PhotoDao = db.getPhotoDao()
}
