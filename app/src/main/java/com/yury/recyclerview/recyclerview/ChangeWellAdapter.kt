package com.yury.recyclerview.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.WellChangeLayoutBinding

class ChangeWellAdapter(val context: Context): RecyclerView.Adapter<ChangeWellAdapter.Holder>(){
    data class ChangeWell(val id: Int, val name: String)

    val changeWellList = ArrayList<ChangeWell>()

    class Holder(item: View): RecyclerView.ViewHolder(item){
        val binding = WellChangeLayoutBinding.bind(item)
        val item = binding.root

        fun bind(item: ChangeWell){
            binding.wellNum.text = item.id.toString()
            binding.wellName.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.well_change_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return changeWellList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(changeWellList[position])
    }

    fun addElement(changeWell: ChangeWell){
        changeWellList.add(changeWell)
        notifyDataSetChanged()
    }
}
