package com.yury.recyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.DivisionDB
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityDivisionBinding
import com.yury.recyclerview.entity.Division
import com.yury.recyclerview.recyclerview.DivisionAdapter

class DivisionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDivisionBinding
    private lateinit var divisionAdapter: DivisionAdapter
    private lateinit var mainDB: MainDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDivisionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.divisionRv.layoutManager = LinearLayoutManager(this@DivisionActivity)
        divisionAdapter = DivisionAdapter(this@DivisionActivity)
        binding.divisionRv.adapter = divisionAdapter

        mainDB = MainDB.getDB(this)
        readDivisionFromDB()

        binding.btnDivInit.setOnClickListener { divisionInit() }

        binding.btnDivSave.setOnClickListener { divisionSave() }
    }

    private fun readDivisionFromDB(){
        mainDB.getDao().getAllDivision().asLiveData().observe(this){
            it.forEach{
                val id = "${it.id}".toInt()
                val name = "${it.name}"
                divisionAdapter.addDivision(Division(id, name))
            }
        }
    }

    private fun divisionSave() {
        /*val divisionName = ""
        Thread {
            var division = DivisionDB(null, divisionName)
            mainDB.getDao().insertDivision(division)
        }.start()*/
    }

    private fun divisionDelete() {
        //Thread{ mainDB.getDao().deleteAllDivisions() }.start()
    }

    private fun divisionInit() {
        // Список месторождений
        Thread{
            var division = DivisionDB(null, "Средний Назым")
            mainDB.getDao().insertDivision(division)
            division = DivisionDB(null, "Средний Хулым")
            mainDB.getDao().insertDivision(division)
            division = DivisionDB(null, "М-р им. В.Н. Виноградова")
            mainDB.getDao().insertDivision(division)
            division = DivisionDB(null, "Сергинское")
            mainDB.getDao().insertDivision(division)
            division = DivisionDB(null, "ЦТН")
            mainDB.getDao().insertDivision(division)
        }.start()
    }
}