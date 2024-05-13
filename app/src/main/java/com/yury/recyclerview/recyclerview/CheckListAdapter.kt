package com.yury.recyclerview.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.yury.recyclerview.Models.SharedModel
import com.yury.recyclerview.R
import com.yury.recyclerview.databinding.CheckLayoutBinding
import com.yury.recyclerview.entity.CheckList

class CheckListAdapter(
    val context: Context,
    var checkItemList : ArrayList<CheckList>

): RecyclerView.Adapter<CheckListAdapter.CheckListHolder>() {

    //private lateinit var sharedModel : SharedModel
    private lateinit var checkOnItem: ArrayList<Boolean>

    /*fun setSharedModel(videoModel: SharedModel){
        sharedModel = videoModel
    }*/

    class CheckListHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = CheckLayoutBinding.bind(item)
        val item = binding.root

        fun bind(checkItem: CheckList){
            binding.numCheck.text = checkItem.idCheckList.toString()
            binding.nameCheck.text = checkItem.nameCheckList
            binding.definitionCheck.text = checkItem.definitionCheck
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.check_layout, parent, false)
        return CheckListHolder(view)
    }

    override fun getItemCount(): Int {
        return checkItemList.size
    }

    override fun onBindViewHolder(holder: CheckListHolder, position: Int) {
        holder.bind(checkItemList[position])


        holder.binding.switchCheck.setOnClickListener {
            if (holder.binding.switchCheck.isChecked){
                Toast.makeText(context, checkOnItem.size.toString(), Toast.LENGTH_LONG).show()
                checkOnItem[position] = true
                Toast.makeText(context, "Нарушение сохранено", Toast.LENGTH_SHORT).show()
            }else{
                checkOnItem[position] = false
                Toast.makeText(context, "Нарушение убрано", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun setCheckOnItem(checkOnItem: ArrayList<Boolean>){
        this.checkOnItem = checkOnItem
    }

    fun getCheckOnItem(position: Int): Boolean{
        return checkOnItem[position]
    }

    /*fun addCheckItem(checkItem: CheckList){
        checkItemList.add(checkItem)
        checkOnItem.add(false)
        notifyDataSetChanged()
    }

    fun updateList(newList : ArrayList<CheckList>){
        checkItemList = newList
        notifyDataSetChanged()
    }*/
}