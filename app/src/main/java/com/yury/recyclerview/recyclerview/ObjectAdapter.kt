package com.yury.recyclerview.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.LocationActivity
import com.yury.recyclerview.R
import com.yury.recyclerview.WellStockActivity
import com.yury.recyclerview.databinding.ObjectLayoutBinding
import com.yury.recyclerview.entity.CheckObject

class ObjectAdapter(val context: Context): RecyclerView.Adapter<ObjectAdapter.ObjectHolder>() {
    val objectList = ArrayList<CheckObject>()

    class ObjectHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = ObjectLayoutBinding.bind(item)
        val item = binding.root

        fun bind(checkObject: CheckObject){
            binding.objectNum.text = checkObject.idObject.toString()
            binding.objectName.text = checkObject.nameObject.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.object_layout, parent, false)
        return ObjectHolder(view)
    }

    override fun getItemCount(): Int {
        return objectList.size
    }

    override fun onBindViewHolder(holder: ObjectHolder, position: Int) {
        holder.bind(objectList[position])

        holder.item.setOnClickListener {
            val intent = Intent(context, WellStockActivity :: class.java)
            intent.putExtra("nameLocation", objectList[position].nameLocation)
            intent.putExtra("nameObject", objectList[position].nameObject)
            intent.putExtra("currentDate", objectList[position].currentDate)
            intent.putExtra("numberCheck", objectList[position].numberCheck)
            context.startActivity(intent)
        }
    }

    fun addCheckObject(checkObject: CheckObject){
        objectList.add(checkObject)
        notifyDataSetChanged()
    }
}