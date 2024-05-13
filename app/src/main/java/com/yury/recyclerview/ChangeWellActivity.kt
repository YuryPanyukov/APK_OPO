package com.yury.recyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.HazardousProdFacility
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityChangeWellBinding
import com.yury.recyclerview.recyclerview.ChangeWellAdapter

class ChangeWellActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeWellBinding
    private lateinit var changeWellAdapter: ChangeWellAdapter
    private lateinit var mainDB: MainDB
    private lateinit var nameDivision: String
    private lateinit var nameObject: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeWellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.changeWellRv.layoutManager = LinearLayoutManager(this@ChangeWellActivity)
        changeWellAdapter = ChangeWellAdapter(this@ChangeWellActivity)
        binding.changeWellRv.adapter = changeWellAdapter

        nameDivision = intent.getStringExtra("nameDivision").toString()
        nameObject   = intent.getStringExtra("nameObject").toString()

        binding.currentLocation.text = nameDivision + ", " + nameObject

        mainDB = MainDB.getDB(this)
        readWellDB()

        binding.buttonInit.setOnClickListener { initWellDB() }

        binding.buttonSave.setOnClickListener { saveWellDB() }
    }

    private fun saveWellDB() {
        Thread{
            var name = binding.nameNewWell.text.toString()
            var tmpWell = HazardousProdFacility(
                null,
                name,
                nameDivision,
                nameObject
            )

            mainDB.getDao().insertHazardPF(tmpWell)

            binding.nameNewWell.setText("")
        }.start()

    }

    private fun initWellDB() {
        if (nameDivision == "Средний Назым"){
            if (nameObject == "КП-10") {
                Thread{
                    var tempWell = HazardousProdFacility(null, "Скв. 1806/10", nameDivision, nameObject)
                    mainDB.getDao().insertHazardPF(tempWell)
                    tempWell.name = "Скв.1807/10"
                    mainDB.getDao().insertHazardPF(tempWell)
                    tempWell.name = "Скв.1808/10"
                    mainDB.getDao().insertHazardPF(tempWell)
                }.start()
            }
            if (nameObject == "КП-11") {
                Thread{
                    var tempWell = HazardousProdFacility(null, "Скв. 1908/11", nameDivision, nameObject)
                    mainDB.getDao().insertHazardPF(tempWell)
                    tempWell.name = "Скв. 2109/11"
                    mainDB.getDao().insertHazardPF(tempWell)
                    tempWell.name = "Скв. 2208/11"
                    mainDB.getDao().insertHazardPF(tempWell)
                }.start()
            }
        }
    }

    private fun readWellDB() {
        mainDB.getDao().getAllHazards().asLiveData().observe(this){
            it.forEach{
                val id = "${it.id}".toInt()
                val name = "${it.name}"
                val currentDivision = "${it.nameDivision}"
                val currentObject = "${it.nameObject}"

                if (currentDivision == nameDivision &&
                    currentObject == nameObject){
                    changeWellAdapter.addElement(ChangeWellAdapter.ChangeWell(
                        id,
                        name
                    ))
                }
            }
        }
    }
}