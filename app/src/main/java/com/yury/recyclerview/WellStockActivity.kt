package com.yury.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityWellStockBinding
import com.yury.recyclerview.entity.WellObject
import com.yury.recyclerview.recyclerview.WellAdapter

class WellStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWellStockBinding
    private lateinit var wellAdapter: WellAdapter
    private lateinit var mainDB: MainDB
    private lateinit var nameDivision: String
    private lateinit var nameObject: String
    private lateinit var currentDate: String
    private lateinit var numberCheck: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDate = intent.getStringExtra("currentDate").toString()
        nameDivision = intent.getStringExtra("nameLocation").toString()
        nameObject = intent.getStringExtra("nameObject").toString()
        numberCheck = intent.getStringExtra("numberCheck").toString()

        binding.FullName.text = nameDivision + " / " + nameObject

        binding.wellStockRv.layoutManager = LinearLayoutManager(this@WellStockActivity)
        wellAdapter = WellAdapter(this@WellStockActivity)
        binding.wellStockRv.adapter = wellAdapter

        mainDB = MainDB.getDB(this)
        readWellDB()

        binding.button.setOnClickListener {
            //wellAdapter.addWell(WellObject(1, "Скв. 1938/10", "КП-10"))
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
                        wellAdapter.addWell(WellObject(id, name, nameObject, nameDivision, currentDate, numberCheck))
                    }
            }
        }
    }
}