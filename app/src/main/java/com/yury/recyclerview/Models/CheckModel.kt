package com.yury.recyclerview.Models

class CheckModel  {
    private var nameWell : String = ""
    private var nameCheck : String = ""

    constructor()
    constructor(nameWell: String, nameCheck: String) {
        this.nameWell = nameWell
        this.nameCheck = nameCheck
    }

    fun getNameWell() : String{
        return nameWell
    }

    fun getNameCheck() : String{
        return nameCheck
    }

    fun setNameWell(nameWell: String){
        this.nameWell = nameWell
    }

    fun setNameCheck(nameCheck: String){
        this.nameCheck = nameCheck
    }
}
