package com.yury.recyclerview.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.CheckActivity
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.WellObjectBinding
import com.yury.recyclerview.entity.WellObject

class WellAdapter(val context: Context): RecyclerView.Adapter<WellAdapter.WellHolder>() {
    val wellList = ArrayList<WellObject>()

    class WellHolder(item: View): RecyclerView.ViewHolder(item){
        val binding = WellObjectBinding.bind(item)
        val item = binding.root

        fun bind(wellObject: WellObject){
            binding.wellNum.text = wellObject.idWell.toString()
            binding.wellName.text = wellObject.nameWell.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WellHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.well_object, parent, false)
        return WellHolder(view)
    }

    override fun getItemCount(): Int {
        return wellList.size
    }

    override fun onBindViewHolder(holder: WellHolder, position: Int) {
        holder.bind(wellList[position])

        holder.item.setOnClickListener {
            val intent = Intent(context, CheckActivity :: class.java)
            intent.putExtra("nameLocation", wellList[position].nameLocation)
            intent.putExtra("nameObject", wellList[position].nameObject)
            intent.putExtra("nameWell", wellList[position].nameWell)
            intent.putExtra("currentDate", wellList[position].currentDate)
            intent.putExtra("numberCheck", wellList[position].numberCheck)
            context.startActivity(intent)
        }
    }

    fun addWell(wellObject: WellObject){
        wellList.add(wellObject)
        notifyDataSetChanged()
    }
}