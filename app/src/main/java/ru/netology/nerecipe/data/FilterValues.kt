package ru.netology.nerecipe.data

class FilterValues(val categories: ArrayList<Int> = ArrayList(),
                   var userid: Long = 0L,
                   var grade: Int = 0,
                   var withComments: Boolean = false,
                   var favoriteSelected:Boolean = false) {

    fun clearValues() {
        categories.clear()
        userid = 0L
        grade = 0
        withComments = false

    }

}

