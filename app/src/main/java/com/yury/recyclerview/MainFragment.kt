package com.yury.recyclerview

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.yury.recyclerview.database.DivisionDB
import com.yury.recyclerview.database.HazardousProdFacility
import com.yury.recyclerview.database.Item
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.database.ObjectDB
import com.yury.recyclerview.databinding.FragmentMainBinding
import com.yury.recyclerview.entity.Plant
import com.yury.recyclerview.recyclerview.PlantAdapter
import kotlin.math.max

class MainFragment : Fragment() {
    private lateinit var binding : FragmentMainBinding
    private lateinit var plantAdapter: PlantAdapter
    private lateinit var mainDB: MainDB
    private lateinit var allCheck: ArrayList<Plant>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.mainRV.layoutManager = LinearLayoutManager(requireContext())
        plantAdapter = PlantAdapter(requireContext())
        binding.mainRV.adapter = plantAdapter

        mainDB = MainDB.getDB(requireContext())
        readStatisticDB()

        binding.buttonAdd.setOnClickListener { creatNewCheck() }
        binding.buttonInit.setOnClickListener { initAllObject() }
        binding.buttonDelete.setOnClickListener { dropAllDB() }

        return binding.root
    }

    private fun readStatisticDB(){
        allCheck = ArrayList()
        var maxNum = 1
        mainDB.getDao().getAllStatistics().asLiveData().observe(viewLifecycleOwner){
            var numPrevious = 0
            it.forEach{
                val numCurrent = "${it.number}".toInt()
                maxNum = max(maxNum, numCurrent)
                val nameDivision = "${it.nameDivision}"
                if (numCurrent != numPrevious){
                    allCheck.add(Plant(numCurrent, nameDivision))
                    numPrevious = numCurrent
                }
            }

            for (item in allCheck) {
                plantAdapter.addPlant(item)
            }
        }

    }

    private fun dropAllDB() {
        Thread{
            mainDB.getDao().deleteAllItems()
            mainDB.getDao().deleteAllDivisions()
            mainDB.getDao().deleteAllHazards()
            mainDB.getDao().deleteAllObjects()
            mainDB.getDao().deleteAllStatistics()
        }.start()
    }

    private fun initAllObject() {
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

        Thread {
            var nameLocation = "Средний Назым"
            var tempObject = ObjectDB(null, "КП-10", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            var tempWell = HazardousProdFacility(null, "Скв. 1806/10", nameLocation, "КП-10")
            mainDB.getDao().insertHazardPF(tempWell)
            tempWell.name = "Скв.1807/10"
            mainDB.getDao().insertHazardPF(tempWell)
            tempWell.name = "Скв.1808/10"
            mainDB.getDao().insertHazardPF(tempWell)

            tempObject = ObjectDB(null, "КП-11", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempWell = HazardousProdFacility(null, "Скв. 1908/11", nameLocation, "КП-11")
            mainDB.getDao().insertHazardPF(tempWell)
            tempWell.name = "Скв. 2109/11"
            mainDB.getDao().insertHazardPF(tempWell)
            tempWell.name = "Скв. 2208/11"
            mainDB.getDao().insertHazardPF(tempWell)

            tempObject = ObjectDB(null, "КП-20", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-29", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-36", nameLocation)
            mainDB.getDao().insertObject(tempObject)
        }.start()

        Thread {
            var nameLocation = "Средний Хулым"
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

        Thread {
            var nameLocation = "Сергинское"
            var tempObject = ObjectDB(null, "КП-1", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-2", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-3", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-9", nameLocation)
            mainDB.getDao().insertObject(tempObject)
        }.start()

        Thread {
            var nameLocation = "М-р им. В.Н. Виноградова"
            var tempObject = ObjectDB(null, "КП-15", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-202", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-37", nameLocation)
            mainDB.getDao().insertObject(tempObject)
            tempObject = ObjectDB(null, "КП-16", nameLocation)
            mainDB.getDao().insertObject(tempObject)
        }.start()

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

    private fun creatNewCheck() {
        mainDB.getDao().getAllStatistics().asLiveData().observe(viewLifecycleOwner) {
            var maxNum = 1
            it.forEach {
                val num = "${it.number}".toInt()
                maxNum = max(num, maxNum)
            }
            val intent = Intent(context, CreateCheckActivity::class.java)
            intent.putExtra("numberCheck", maxNum.toString())
            startActivity(intent)
        }

        /*mainDB.getDao().getAllStatistics().asLiveData().observe(viewLifecycleOwner){
            var maxNum = 1
            it.forEach{
                val num = "${it.number}".toInt()
                maxNum = max(num, maxNum)
            }
            plantAdapter.addPlant(Plant(maxNum+1, "Empty"))
        }*/
    }
}