package com.yury.recyclerview

import android.R
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.asLiveData
import com.yury.recyclerview.database.MainDB
import com.yury.recyclerview.databinding.ActivityLocationBinding
import java.io.File
import java.util.Date

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var mainDB: MainDB
    private lateinit var currentLocation: String
    private lateinit var numberCheck: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainDB = MainDB.getDB(this)

        numberCheck = intent.getStringExtra("numberCheck").toString()
        currentLocation = intent.getStringExtra("currentLocation").toString()

        binding.btnGoCheck.setOnClickListener {
            val intent = Intent(this@LocationActivity, BushPlaygroundActivity :: class.java)
            intent.putExtra("currentDate", binding.timeView.text)
            intent.putExtra("currentLocation", currentLocation)
            intent.putExtra("numberCheck", numberCheck)
            startActivity(intent)
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val currentDate = sdf.format(Date())

        binding.timeView.text = currentDate
        binding.CurrentLocation.text = currentLocation

        binding.getReport.setOnClickListener {
            val intent = Intent(this@LocationActivity, GetCheckActivity::class.java)
            intent.putExtra("numberCheck", numberCheck)
            startActivity(intent)
            //createReport()
        }
    }

    private fun createReport() {
        mainDB.getDao().getAllStatistics().asLiveData().observe(this){
            it.forEach{
                val numCheck = "${it.number}"
                if (numCheck == numberCheck){
                    val currObject = "${it.nameObject}"
                    val currWell = "${it.nameWell}"
                    val nameCheck = "${it.nameCheck}"
                    val nameDef = "${it.checkDef}"
                    binding.report.append(currObject + " " + currWell + ": " + nameCheck + " - " + nameDef + "\n")
                }
            }

        }
        var text = binding.report.text
        //Toast.makeText(this, names, Toast.LENGTH_LONG).show()

        val data: String = text.toString()
        val path = this.getExternalFilesDir(null)

        val folder = File(path, "avalakki")
        folder.mkdirs()

        val file = File(folder, "file_name.txt")
        file.appendText("$data")

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "type/palin"
        val sharedBody = "You are body"
        val sharedSub = data
        intent.putExtra(Intent.EXTRA_SUBJECT, sharedBody)
        intent.putExtra(Intent.EXTRA_TEXT, sharedSub)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.path))
        startActivity(Intent.createChooser(intent, "share you App"))
    }

}