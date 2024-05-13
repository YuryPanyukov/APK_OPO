package com.yury.recyclerview.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatisticDB(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "number")
    var number: Int,
    @ColumnInfo(name = "Division")
    var nameDivision: String,
    @ColumnInfo(name = "Object")
    var nameObject: String,
    @ColumnInfo(name = "Well")
    var nameWell: String,
    @ColumnInfo(name = "check")
    var nameCheck: String,
    @ColumnInfo(name = "Definition")
    var checkDef: String
)
