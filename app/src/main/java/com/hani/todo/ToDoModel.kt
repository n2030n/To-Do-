package com.hani.todo

class ToDoModel{
    companion object{
        fun createList() : ToDoModel = ToDoModel()
    }

    var UID: String? = null
    var taskTitle : String? = null
    var dueDate : String? = null
    var done : Boolean? = null
}
