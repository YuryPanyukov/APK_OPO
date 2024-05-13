package com.yury.recyclerview.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    fun insertItem(item: Item)
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>
    @Query("DELETE FROM items")
    fun deleteAllItems()

    @Insert
    fun insertDivision(division: DivisionDB)
    @Query("SELECT * FROM division")
    fun getAllDivision(): Flow<List<DivisionDB>>
    @Query("DELETE FROM division")
    fun deleteAllDivisions()

    @Insert
    fun insertObject(objectDB: ObjectDB)
    @Query("SELECT * FROM object")
    fun getAllObjects(): Flow<List<ObjectDB>>
    @Query("DELETE FROM object")
    fun deleteAllObjects()

    @Insert
    fun insertHazardPF(hazardousProdFacility: HazardousProdFacility)
    @Query("SELECT * FROM hazardous")
    fun getAllHazards(): Flow<List<HazardousProdFacility>>
    @Query("DELETE FROM hazardous")
    fun deleteAllHazards()

    @Insert
    fun insertStatistics(statisticDB: StatisticDB)
    @Query("SELECT * FROM statistics")
    fun getAllStatistics(): Flow<List<StatisticDB>>
    @Query("DELETE FROM statistics")
    fun deleteAllStatistics()
}