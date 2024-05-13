package com.yury.recyclerview.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.LocationActivity
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.MsgItemLayoutBinding
import com.yury.recyclerview.entity.Plant

class PlantAdapter(val context: Context): RecyclerView.Adapter<PlantAdapter.PlantHolder>() {
    val plantList = ArrayList<Plant>()

    class PlantHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = MsgItemLayoutBinding.bind(item)
        val item = binding.root

        fun bind(plant: Plant){
            binding.msgImage.setImageResource(R.drawable.banner_4)
            binding.msgNum.text = plant.num.toString()
            binding.msgTv.text = plant.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_item_layout, parent, false)
        return PlantHolder(view)
    }

    override fun onBindViewHolder(holder: PlantHolder, position: Int) {
        holder.bind(plantList[position])

        holder.item.setOnClickListener {
            val intent = Intent(context, LocationActivity :: class.java)
            intent.putExtra("numberCheck", plantList[position].num.toString())
            intent.putExtra("currentLocation", plantList[position].title)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return plantList.size
    }

    fun addPlant(plant: Plant){
        plantList.add(plant)
        notifyDataSetChanged()
    }
}