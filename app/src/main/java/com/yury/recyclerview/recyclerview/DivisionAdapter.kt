package com.yury.recyclerview.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.ChangeObjectActivity
import com.yury.recyclerview.R
import com.yury.recyclerview.WellStockActivity
import com.yury.recyclerview.databinding.DivisionLayoutBinding
import com.yury.recyclerview.entity.Division

class DivisionAdapter(val context: Context): RecyclerView.Adapter<DivisionAdapter.DivisionHolder>() {
    val divisionList = ArrayList<Division>()

    class DivisionHolder(item: View): RecyclerView.ViewHolder(item){
        val binding = DivisionLayoutBinding.bind(item)
        val item = binding.root

        fun bind(division: Division){
            binding.numDivision.text = division.idDivision.toString()
            binding.nameDivision.text = division.nameDivison.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DivisionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.division_layout, parent, false)
        return DivisionHolder(view)
    }

    override fun getItemCount(): Int {
        return divisionList.size
    }

    override fun onBindViewHolder(holder: DivisionHolder, position: Int) {
        holder.bind(divisionList[position])

        holder.item.setOnClickListener {
            val intent = Intent(context, ChangeObjectActivity :: class.java)
            intent.putExtra("nameLocation", divisionList[position].nameDivison)
            context.startActivity(intent)
        }
    }

    fun addDivision(division: Division){
        divisionList.add(division)
        notifyDataSetChanged()
    }
}