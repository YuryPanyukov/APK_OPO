package com.yury.recyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.DivisionDB
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.database.ObjectDB
import com.yury.recyclerview.databinding.ActivityChangeObjectBinding
import com.yury.recyclerview.entity.Division
import com.yury.recyclerview.recyclerview.ChangeObjectAdapter

class ChangeObjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeObjectBinding
    private lateinit var changeObjectAdapter: ChangeObjectAdapter
    private lateinit var mainDB: MainDB
    private lateinit var nameLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameLocation = intent.getStringExtra("nameLocation").toString()
        binding.currentLocation.text = nameLocation

        binding.changeObjectRv.layoutManager = LinearLayoutManager(this@ChangeObjectActivity)
        changeObjectAdapter = ChangeObjectAdapter(this@ChangeObjectActivity, nameLocation)
        binding.changeObjectRv.adapter = changeObjectAdapter

        mainDB = MainDB.getDB(this)
        readChangeObjectFromDB()

        binding.buttonSave.setOnClickListener { saveObjectDB() }
        binding.buttonInit.setOnClickListener { initObjectDB() }
    }

    private fun initObjectDB() {
        // Список месторождений
        if (nameLocation == "Средний Назым") {
            Thread {
                var tempObject = ObjectDB(null, "КП-10", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-11", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-20", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-29", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-36", nameLocation)
                mainDB.getDao().insertObject(tempObject)
            }.start()
        }
        if (nameLocation == "Средний Хулым") {
            Thread {
                var tempObject = ObjectDB(null, "КП-1", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-2", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-3", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-5", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-12бис", nameLocation)
                mainDB.getDao().insertObject(tempObject)
            }.start()
        }
        if (nameLocation == "Сергинское") {
            Thread {
                var tempObject = ObjectDB(null, "КП-1", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-2", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-3", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-9", nameLocation)
                mainDB.getDao().insertObject(tempObject)
            }.start()
        }

        if (nameLocation == "М-р им. В.Н. Виноградова") {
            Thread {
                var tempObject = ObjectDB(null, "КП-15", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-202", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-37", nameLocation)
                mainDB.getDao().insertObject(tempObject)
                tempObject = ObjectDB(null, "КП-16", nameLocation)
                mainDB.getDao().insertObject(tempObject)
            }.start()
        }
    }

    private fun saveObjectDB() {
        Thread {
            var name = binding.nameNewObject.text.toString()
            var tempObject = ObjectDB(null, name, nameLocation)
            mainDB.getDao().insertObject(tempObject)

            binding.nameNewObject.setText("")
        }.start()
    }

    private fun readChangeObjectFromDB() {
        mainDB.getDao().getAllObjects().asLiveData().observe(this){
            it.forEach{
                val id = "${it.id}".toInt()
                val name = "${it.name}"
                val currentLocation = "${it.nameDivision}"
                if (currentLocation == nameLocation){
                    changeObjectAdapter.addElement(ChangeObjectAdapter.ChangeObject(id, name))
                }
            }
        }
    }
}