package com.yury.recyclerview.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.ChangeObjectActivity
import com.yury.recyclerview.ChangeWellActivity
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.ObjectChangeLayoutBinding

// Кустовая площадка
class ChangeObjectAdapter(val context: Context, val _nameDivision: String):RecyclerView.Adapter<ChangeObjectAdapter.Holder>() {
    data class ChangeObject(val id: Int, val name: String)

    val changeObjectList = ArrayList<ChangeObject>()
    val nameDivision = _nameDivision

    class Holder(item: View):RecyclerView.ViewHolder(item){
        val binding = ObjectChangeLayoutBinding.bind(item)
        val item = binding.root

        fun bind(item: ChangeObject){
            binding.objectNum.text = item.id.toString()
            binding.objectName.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.object_change_layout, parent, false)
        return Holder(view)

    }

    override fun getItemCount(): Int {
        return changeObjectList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(changeObjectList[position])

        holder.item.setOnClickListener {
            val intent = Intent(context, ChangeWellActivity :: class.java)
            intent.putExtra("nameDivision", nameDivision)
            intent.putExtra("nameObject", changeObjectList[position].name)
            context.startActivity(intent)
        }
    }

    fun addElement(changeObject: ChangeObject){
        changeObjectList.add(changeObject)
        notifyDataSetChanged()
    }
}