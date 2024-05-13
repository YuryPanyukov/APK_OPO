package com.yury.recyclerview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.Item
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityCheckListBinding
import com.yury.recyclerview.entity.CheckList
import com.yury.recyclerview.recyclerview.AmazingCheckListAdapter

class CheckListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckListBinding
    private lateinit var amazingAdapter: AmazingCheckListAdapter
    private lateinit var mainDB: MainDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkListRv.layoutManager = LinearLayoutManager(this@CheckListActivity)
        amazingAdapter = AmazingCheckListAdapter(this@CheckListActivity)
        binding.checkListRv.adapter = amazingAdapter

        mainDB = MainDB.getDB(this)
        readCheckListDB()

        binding.initButton.setOnClickListener { initCheckListDB() }

        binding.saveButton.setOnClickListener { insertCheckListDB() }

        binding.deleteButton.setOnClickListener { deleteCheckListDB() }
    }

    private fun initCheckListDB() {
        Thread{
            val item = Item(
                null,
                "Отсутствует «Красная стрелка» на шкале манометра",
                "Нарушение: п.565. ФН и П в ОПБ «Правила безопасности в нефтяной и газовой промышленности», утв. приказом Ростехнадзора от 15.12.2020 г. № 534.")
            mainDB.getDao().insertItem(item)
            item.name = "Отсутствует трехходовой кран перед манометром"
            mainDB.getDao().insertItem(item)
            item.name = "Допускается эксплуатация манометра, с превышением допустимого верхнего значения диапазона показаний"
            mainDB.getDao().insertItem(item)
            item.name = "Истек срок поверки манометра"
            mainDB.getDao().insertItem(item)

            item.name = "Допускается эксплуатация шин заземления без цветового обозначения"
            item.definition = "Нарушение: Глава 1.1 «Правила устройства электроустановок (ПУЭ)», п.1.1.29"
            mainDB.getDao().insertItem(item)

            item.name = "Допускается использование огнетушителя с значением манометра в Красной зоне"
            item.definition = "Нарушение: СП 9.13130.2009, п.4.3.5"
            mainDB.getDao().insertItem(item)

            item.name = "При проведении проверки Газоанализатора на сработку на газ, нет сработки"
            item.definition = "П.П.564, 659 ФЕДЕРАЛЬНЫХ НОРМ И ПРАВИЛ В ОБЛАСТИ ПРОМЫШЛЕННОЙ БЕЗОПАСНОСТИ «ПРАВИЛ БЕЗОПАСНОСТИ В НЕФТЯНОЙ И ГАЗОВОЙ ПРОМЫШЛЕННОСТИ»."
            mainDB.getDao().insertItem(item)
        }.start()
    }

    private fun deleteCheckListDB() {
        Thread{
            mainDB.getDao().deleteAllItems()
        }.start()
    }

    private fun insertCheckListDB() {
        val item = Item(
            null,
            binding.nameCheck.text.toString(),
            binding.defCheck.text.toString()
        )
        Thread{
            mainDB.getDao().insertItem(item)
        }.start()
        binding.nameCheck.setText("")
        binding.defCheck.setText("")
    }

    private fun readCheckListDB() {
        mainDB.getDao().getAllItems().asLiveData().observe(this){
            it.forEach{
                val id = "${it.id}".toInt()
                val name = "${it.name}"
                val definition = "${it.definition}"
                amazingAdapter.addElement(CheckList(id, name, definition, numberCheck = "-1"))
            }
        }
    }
}