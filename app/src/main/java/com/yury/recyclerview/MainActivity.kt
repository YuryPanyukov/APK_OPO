package com.yury.recyclerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityMainBinding
import com.yury.recyclerview.entity.Plant
import com.yury.recyclerview.recyclerview.PlantAdapter
import kotlin.math.max
import androidx.navigation.ui.setupWithNavController


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var plantAdapter: PlantAdapter
    private lateinit var mainDB: MainDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(MainFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mainFragment -> replaceFragment(MainFragment())
                R.id.checkFragment -> replaceFragment(CheckFragment())
                R.id.divisionFragment -> replaceFragment(DivisionFragment())

                else ->{}
            }
            true
        }

        /*binding.bottomNavigationView.setOnClickListener {
            when(it.itemId){
                R.id.mainFragment -> replaceFragment(MainFragment())
                R.id.checkFragment -> replaceFragment(CheckFragment())
                R.id.mainFragment -> replaceFragment(DivisionFragment())
            }
        }*/

        //val navigationView = findNavController(R.id.container)

        //bottomNavView.setupWithNavController(navigationView)

        /*binding.messageRv.layoutManager = LinearLayoutManager(this@MainActivity)
        plantAdapter = PlantAdapter(this@MainActivity)
        binding.messageRv.adapter = plantAdapter

        mainDB = MainDB.getDB(this)

        binding.btnNewCheck.setOnClickListener { creatNewCheck() }

        binding.btnToDivision.setOnClickListener {
            val intent = Intent(this@MainActivity, DivisionActivity :: class.java)
            startActivity(intent)
        }

        binding.btnToChecklist.setOnClickListener {
            val intent = Intent(this@MainActivity, CheckListActivity :: class.java)
            startActivity(intent)
        }*/
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }

    private fun creatNewCheck() {
        mainDB.getDao().getAllStatistics().asLiveData().observe(this){
            var maxNum = 1
            it.forEach{
                val num = "${it.number}".toInt()
                maxNum = max(num, maxNum)
            }
            plantAdapter.addPlant(Plant(maxNum, "Hello"))
        }
    }
}