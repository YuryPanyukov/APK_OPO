package com.yury.recyclerview.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.DivisionLayoutBinding
import com.yury.recyclerview.entity.UniversalObeject

class DivisionFragmentAdapter(
    val context: Context,
    val divisionList: ArrayList<UniversalObeject>
): RecyclerView.Adapter<DivisionFragmentAdapter.DivisionFragmentHolder>() {
    class DivisionFragmentHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = DivisionLayoutBinding.bind(item)
        val item = binding.root

        fun bind(universalObeject: UniversalObeject){
            binding.numDivision.text = universalObeject.num.toString()
            binding.nameDivision.text = universalObeject.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DivisionFragmentHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.division_layout, parent, false)
        return DivisionFragmentHolder(view)
    }

    override fun getItemCount(): Int {
        return divisionList.size
    }

    override fun onBindViewHolder(holder: DivisionFragmentHolder, position: Int) {
        holder.bind(divisionList[position])
    }
}