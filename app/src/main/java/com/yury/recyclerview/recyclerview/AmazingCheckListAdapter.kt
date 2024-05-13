package com.yury.recyclerview.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.CheckListLayoutBinding
import com.yury.recyclerview.entity.CheckList

class AmazingCheckListAdapter(val context: Context): RecyclerView.Adapter<AmazingCheckListAdapter.Holder>() {
    val amazingCheckList = ArrayList<CheckList>()

    class Holder(item: View): RecyclerView.ViewHolder(item){
        val binding = CheckListLayoutBinding.bind(item)
        val item = binding.root

        fun bind(item: CheckList){
            binding.numCheck.text = item.idCheckList.toString()
            binding.nameCheck.text = item.nameCheckList
            binding.definitionCheck.text = item.definitionCheck
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.check_list_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return amazingCheckList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(amazingCheckList[position])
    }

    fun addElement(checkList: CheckList){
        amazingCheckList.add(checkList)
        notifyDataSetChanged()
    }

    fun clear(){
        amazingCheckList.clear()
        notifyDataSetChanged()
    }

}