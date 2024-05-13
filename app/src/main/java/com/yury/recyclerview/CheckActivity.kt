package com.yury.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.Models.SharedModel
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.database.StatisticDB
import com.yury.recyclerview.databinding.ActivityCheckBinding
import com.yury.recyclerview.entity.CheckList
import com.yury.recyclerview.recyclerview.CheckListAdapter

class CheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckBinding
    private lateinit var checkListAdapter: CheckListAdapter
    private lateinit var checkListInner: ArrayList<CheckList>
    private lateinit var checkOnItem: ArrayList<Boolean>
    private lateinit var mainDB: MainDB
    private lateinit var currentDate: String
    private lateinit var nameDivision: String
    private lateinit var nameObject: String
    private lateinit var nameWell: String
    private lateinit var numberCheck: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainDB = MainDB.getDB(this)
        readCheckListDB()

        binding.checkListRv.layoutManager = LinearLayoutManager(this@CheckActivity)
        checkListAdapter = CheckListAdapter(this@CheckActivity, checkListInner)
        checkListAdapter.setCheckOnItem(checkOnItem)
        //sharedModel = ViewModelProvider(this).get(SharedModel :: class.java)
        //checkListAdapter.setSharedModel(sharedModel)
        binding.checkListRv.adapter = checkListAdapter

        currentDate = intent.getStringExtra("currentDate").toString()
        nameDivision = intent.getStringExtra("nameLocation").toString()
        nameObject = intent.getStringExtra("nameObject").toString()
        nameWell = intent.getStringExtra("nameWell").toString()
        numberCheck = intent.getStringExtra("numberCheck").toString()

        binding.FullName.text = nameDivision + " / " + nameObject  + " / " + nameWell


        binding.searchBar.clearFocus()
        //searchFilter()
    }

    override fun onStop() {
        super.onStop()
        Thread{
            for (item in checkListInner) {
                if (checkListAdapter.getCheckOnItem(item.idCheckList-1) == true) {
                    mainDB.getDao().insertStatistics(
                        StatisticDB(
                            null,
                            numberCheck.toInt(),
                            nameDivision,
                            nameObject,
                            nameWell,
                            item.nameCheckList,
                            item.definitionCheck
                        )
                    )
                }
            }
        }.start()
        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show()
    }

    /*private fun searchFilter() {
        binding.searchBar.setOnQueryTextListener(object :
        //what what whwtt
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(input: String?){
        val filteredList = if (input.isNullOrEmpty()){
            checkListInner
        }else{
            checkListInner.filter { item ->
                item.nameCheckList.contains(input, ignoreCase = true)
            }
        }

        checkListAdapter.updateList(filteredList as ArrayList<CheckList>)
    }*/

    private fun readCheckListDB() {
        checkListInner = ArrayList()
        checkOnItem = ArrayList()

        mainDB.getDao().getAllItems().asLiveData().observe(this){
            var currentNum = 1
            it.forEach{
                val name = "${it.name}"
                val definition = "${it.definition}"
                checkListInner.add(CheckList(currentNum, name, definition, numberCheck))
                currentNum++
                checkOnItem.add(false)
            }
        }
    }
}