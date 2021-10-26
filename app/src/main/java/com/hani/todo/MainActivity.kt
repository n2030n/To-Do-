package com.hani.todo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() , UpdateAndDelete {


    lateinit var database: DatabaseReference
    var toDoList: MutableList<ToDoModel>? = null
    lateinit var adapter: ToDoAdapter
    private var listViewItem: ListView? = null
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {

        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val birthday = "$day/$month/$year"



        getSupportActionBar()?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().reference
        fab = findViewById(R.id.fab)
        listViewItem = findViewById(R.id.item_listView)

        fab.setOnClickListener { view ->


            val alertDialog = AlertDialog.Builder(this)
            val textEditText = EditText(this)
            alertDialog.setMessage("Add ToDo item")
            alertDialog.setTitle("Enter ToDo item")
            alertDialog.setView(textEditText)
            alertDialog.setPositiveButton("Add") { dialog, i ->
                val todoItemData = ToDoModel.createList()
                todoItemData.itemDataText = textEditText.text.toString()
                todoItemData.done = false


                val newItemData = database.child("todo").push()
                todoItemData.UID = newItemData.key

                newItemData.setValue(todoItemData)

                dialog.dismiss()
                Toast.makeText(this, "Item Saved", Toast.LENGTH_LONG).show()

            }
            alertDialog.show()
        }
        toDoList = mutableListOf<ToDoModel>()
        adapter = ToDoAdapter(this, toDoList!!)
        listViewItem!!.adapter = adapter

        database.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "No item added", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                toDoList!!.clear()
                addItemToList(snapshot)
            }
        })
    }

    private fun addItemToList(snapshot: DataSnapshot) {
        val items = snapshot.children.iterator()
        if (items.hasNext()) {
            val toDoIndexedValue = items.next()
            val itemsIterator = toDoIndexedValue.children.iterator()

            while(itemsIterator.hasNext()){
                val currentItem = itemsIterator.next()
                val toDoitemDate = ToDoModel.createList()
                val map = currentItem.getValue() as HashMap<String, Any>

                toDoitemDate.UID = currentItem.key
                toDoitemDate.done = map.get("done") as Boolean?
                toDoitemDate.itemDataText = map.get("itemDataText") as String?
                toDoList!!.add(toDoitemDate)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modefyItem(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }
}