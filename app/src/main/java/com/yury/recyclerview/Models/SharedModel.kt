package com.yury.recyclerview.Models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedModel: ViewModel() {
    val cartItem = MutableLiveData<ArrayList<CheckModel>>()
    val buttonStates = ArrayList<Boolean>()

    fun getButtonStates() : List<Boolean>{
        return buttonStates
    }

    fun addToCart(item : CheckModel){
        val currentCartItem = cartItem.value ?: ArrayList<CheckModel>()
        currentCartItem.add(item)
        cartItem.value = currentCartItem
        buttonStates.add(true)
    }

    fun deleteFromCart(item: CheckModel){
        val currentCartItem = cartItem.value ?: ArrayList<CheckModel>()
        val index  =currentCartItem.indexOf(item)
        if (index != -1){
            currentCartItem.removeAt(index)
            cartItem.value = currentCartItem
            buttonStates.removeAt(index)
        }
    }

    fun inList(item: CheckModel) : Boolean{
        val currentCartItem = cartItem.value ?: ArrayList<CheckModel>()
        return currentCartItem.contains(item)
    }
}