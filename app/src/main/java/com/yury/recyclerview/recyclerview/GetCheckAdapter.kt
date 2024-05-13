package com.yury.recyclerview.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.GetCheckLayoutBinding
import com.yury.recyclerview.entity.GetCheck

class GetCheckAdapter(val context: Context): RecyclerView.Adapter<GetCheckAdapter.Holder>() {
    val getCheckList = ArrayList<GetCheck>()
    class Holder(item: View): RecyclerView.ViewHolder(item) {
        val binding = GetCheckLayoutBinding.bind(item)
        val item = binding.root

        fun bind(item: GetCheck){
            binding.numCheck.text = item.numCheck.toString()
            binding.nameCheck.text = item.nameCheck
            binding.nameWell.text = item.nameWell
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.get_check_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return getCheckList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getCheckList[position])
    }

    fun addElement(getCheck: GetCheck){
        getCheckList.add(getCheck)
        notifyDataSetChanged()
    }
}