package com.yury.recyclerview.QRReader

data class QRReader(
    val header: String,
    val objects: ArrayList<String>,
    val wells: ArrayList<ArrayList<String>>
    )

fun text2QRReader(text: String): QRReader {
    var header = ""
    var objects = arrayListOf<String>()
    var wells = arrayListOf<ArrayList<String>>()

    val headerValue = text.split(".")
    if (headerValue.size == 2){
        header = headerValue[0]
        val innerValue = headerValue[1].split(";")
        for (inner in innerValue){
            val objectValue = inner.split(":")
            if (objectValue.size == 2){
                objects.add(objectValue[0])
                val wellValue = objectValue[1].split(",")
                wells.add(ArrayList<String>())
                val idx = wells.size-1
                for (well in wellValue){
                    wells[idx].add(well)
                }
            }
        }
    }

    return QRReader(header, objects, wells)
}