package com.yury.recyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityGetCheckBinding
import com.yury.recyclerview.entity.GetCheck
import com.yury.recyclerview.recyclerview.GetCheckAdapter

class GetCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetCheckBinding
    private lateinit var getCheckAdapter: GetCheckAdapter
    private lateinit var numberCheck: String
    private lateinit var mainDB: MainDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.RVFullCheck.layoutManager = LinearLayoutManager(this@GetCheckActivity)
        getCheckAdapter = GetCheckAdapter(this@GetCheckActivity)
        binding.RVFullCheck.adapter = getCheckAdapter

        numberCheck = intent.getStringExtra("numberCheck").toString()

        mainDB = MainDB.getDB(this)
        readDBCheckItemList()
    }

    private fun readDBCheckItemList() {
        mainDB.getDao().getAllStatistics().asLiveData().observe(this){
            it.forEach{
                val numCheck = "${it.number}"
                if (numCheck == numberCheck){
                    val currObject = "${it.nameObject}"
                    val currWell = "${it.nameWell}"
                    val nameCheck = "${it.nameCheck}"
                    val nameDef = "${it.checkDef}"
                    getCheckAdapter.addElement(GetCheck(1, nameCheck, currWell))
                    //binding.report.append(currObject + " " + currWell + ": " + nameCheck + " - " + nameDef + "\n")
                }
            }

        }
    }
}