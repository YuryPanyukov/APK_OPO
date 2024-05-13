package com.yury.recyclerview.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazardous")
data class HazardousProdFacility(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "nameDivision")
    var nameDivision: String,
    @ColumnInfo(name = "nameObject")
    var nameObject: String
)
