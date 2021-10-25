package com.hani.todo

interface UpdateAndDelete {
    fun modefyItem(itemUID : String , isDone : Boolean)
    fun onItemDelete(itemUID: String)
}