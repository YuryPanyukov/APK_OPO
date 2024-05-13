package com.yury.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityBushPlaygroundBinding
import com.yury.recyclerview.entity.CheckObject
import com.yury.recyclerview.recyclerview.ChangeObjectAdapter
import com.yury.recyclerview.recyclerview.ObjectAdapter

class BushPlaygroundActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBushPlaygroundBinding
    private lateinit var objectAdapter: ObjectAdapter
    private lateinit var mainDB: MainDB
    private lateinit var nameDivision: String
    private lateinit var currentDate: String
    private lateinit var numberCheck: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBushPlaygroundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameDivision = intent.getStringExtra("currentLocation").toString()
        currentDate = intent.getStringExtra("currentDate").toString()
        numberCheck = intent.getStringExtra("numberCheck").toString()

        binding.currentLocation.setText(numberCheck + ". " + nameDivision)
        binding.currentDate.setText(currentDate)

        binding.bushPlayRv.layoutManager = LinearLayoutManager(this@BushPlaygroundActivity)
        objectAdapter = ObjectAdapter(this@BushPlaygroundActivity)
        binding.bushPlayRv.adapter = objectAdapter

        mainDB = MainDB.getDB(this)
        readObjectDB()
    }

    private fun readObjectDB() {
        mainDB.getDao().getAllObjects().asLiveData().observe(this){
            it.forEach{
                val id = "${it.id}".toInt()
                val name = "${it.name}"
                val currentDivision = "${it.nameDivision}"
                if (currentDivision == nameDivision){
                    objectAdapter.addCheckObject(CheckObject(id, name, nameDivision, currentDate, numberCheck))
                }
            }
        }
    }
}