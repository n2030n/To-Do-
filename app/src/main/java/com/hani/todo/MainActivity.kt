package com.hani.todo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), UpdateAndDelete {

    // Creating the current date and store it in creationDate
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val creationDate = current.format(formatter)


    //due date
    val myCalendar = Calendar.getInstance()
    val day = myCalendar.get(Calendar.DAY_OF_MONTH)
    val month = myCalendar.get(Calendar.MONTH)
    val year = myCalendar.get(Calendar.YEAR)


    private lateinit var dueDate: String


    lateinit var database: DatabaseReference
    var toDoList: MutableList<ToDoModel>? = null
    lateinit var adapter: ToDoAdapter
    private var listViewItem: ListView? = null
    private lateinit var fab: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {


        getSupportActionBar()?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().reference
        fab = findViewById(R.id.fab)
        listViewItem = findViewById(R.id.item_listView)

        fab.setOnClickListener {
            //Inflate the dialog with custom view
            val addTaskDialog = LayoutInflater.from(this).inflate(R.layout.add_task_dialog, null)

            val dialogAddBtn = addTaskDialog.findViewById<Button>(R.id.addBtn)
            val dialogCancelBtn = addTaskDialog.findViewById<Button>(R.id.cancelBtn)
            val taskTitleET = addTaskDialog.findViewById<EditText>(R.id.taskTitleET)
            val taskDescriptionET = addTaskDialog.findViewById<EditText>(R.id.taskDescriptionET)
            val dueDateTV = addTaskDialog.findViewById<TextView>(R.id.dueDateTV)


            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(addTaskDialog)
                .setTitle("Add Task")

            dueDateTV.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, y, m, d ->
                        dueDate = "$y/${m + 1}/$d"
                        dueDateTV.setText(dueDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.datePicker.minDate = myCalendar.timeInMillis
                datePickerDialog.show()

            }


            //show dialog
            val mAlertDialog = mBuilder.show()
            //login button click of custom layout
            dialogAddBtn.setOnClickListener {


                val todoItemData = ToDoModel.createList()
                todoItemData.taskTitle = taskTitleET.text.toString()
                todoItemData.dueDate = dueDate
                todoItemData.done = false


                val newItemData = database.child("todo").push()
                todoItemData.UID = newItemData.key



                newItemData.setValue(todoItemData)


                Toast.makeText(this, "Item Saved", Toast.LENGTH_LONG).show()

                mAlertDialog.dismiss()

            }


            //cancel button click of custom layout
            dialogCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }
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

            while (itemsIterator.hasNext()) {
                val currentItem = itemsIterator.next()
                val toDoitemDate = ToDoModel.createList()
                val map = currentItem.getValue() as HashMap<String, Any>

                toDoitemDate.UID = currentItem.key
                toDoitemDate.done = map.get("done") as Boolean?
                toDoitemDate.taskTitle = map.get("taskTitle") as String?
                toDoitemDate.dueDate = map.get("dueDate") as String?
                toDoList!!.add(toDoitemDate)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modefyItem(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
        //itemReference.child("dueDate").setValue(dueDate)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }
}