package com.yury.recyclerview

import android.R
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityCreateCheckBinding
import java.util.Date

class CreateCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateCheckBinding
    private lateinit var currentLocation: String
    private lateinit var mainDB: MainDB
    private lateinit var numberCheck: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainDB = MainDB.getDB(this)

        chooseLocation()
        numberCheck = intent.getStringExtra("numberCheck").toString()

        binding.btnGoCheck.setOnClickListener {
            val intent = Intent(this@CreateCheckActivity, BushPlaygroundActivity :: class.java)
            intent.putExtra("currentDate", binding.timeView.text)
            intent.putExtra("currentLocation", currentLocation)
            intent.putExtra("numberCheck", numberCheck)
            startActivity(intent)
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val currentDate = sdf.format(Date())

        binding.timeView.text = currentDate
    }

    private fun chooseLocation() {
        val locationList = mutableListOf<String>()
        mainDB.getDao().getAllDivision().asLiveData().observe(this){
            it.forEach{
                val name = "${it.name}"
                locationList.add(name)
            }
        }

        val adapter = ArrayAdapter(
            this@CreateCheckActivity,
            R.layout.simple_dropdown_item_1line,
            locationList
        )

        binding.locationList.setAdapter(adapter)

        binding.locationList.setOnItemClickListener { parent, view, position, l ->
            currentLocation = parent.getItemAtPosition(position) as String
        }
    }
}