package com.hani.todo

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

var currentDateAndTime = LocalDateTime.now()
var formatTheDateAndTime = DateTimeFormatter.ofPattern("yyyy-MM-dd")
var currentDate = currentDateAndTime.format(formatTheDateAndTime)
var selectedDueDate = currentDate.toString()


class MainActivity : AppCompatActivity(), UpdateAndDelete {

    // Date
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatted = current.format(formatter)


    //due date
    val c = Calendar.getInstance()
    val day = c.get(Calendar.DAY_OF_MONTH)
    val month = c.get(Calendar.MONTH)
    val year = c.get(Calendar.YEAR)


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
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_task_dialog, null)

            val dialogAddBtn = mDialogView.findViewById<Button>(R.id.addBtn)
            val dialogCancelBtn = mDialogView.findViewById<Button>(R.id.cancelBtn)
            val taskTitleET = mDialogView.findViewById<EditText>(R.id.taskTitleET)
            val taskDescriptionET = mDialogView.findViewById<EditText>(R.id.taskDescriptionET)
            val dueDateTV = mDialogView.findViewById<TextView>(R.id.dueDateTV)


            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Login Form")


            //show dialog
            val mAlertDialog = mBuilder.show()
            //login button click of custom layout
            dialogAddBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val name = taskTitleET.text.toString()
                val email = taskDescriptionET.text.toString()
                val password = dueDateTV.text.toString()
                //set the input text in TextView
                //mainInfoTv.setText("Name:"+ name +"\nEmail: "+ email +"\nPassword: "+ password)
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
                toDoitemDate.itemDataText = map.get("itemDataText") as String?
                toDoitemDate.dueDate = map.get("dueDate") as String?
                toDoList!!.add(toDoitemDate)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modefyItem(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
        itemReference.child("dueDate").setValue(selectedDueDate)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }

//    fun addTaskDailog() {
//        //Inflate the dialog with custom view
//        val addTask = android.app.AlertDialog.Builder(view?.context)
//        val myView: View = layoutInflater.inflate(R.layout.add_tasks, null)
//
//        addTask.setView(myView)
//        addTask.setTitle("Add Task")
//        val Save: Button = myView.findViewById(R.id.btnSave)
//        val cancel: Button = myView.findViewById(R.id.btnCancel)
//        val dateAlerAddTask: TextView = myView.findViewById(R.id.tvDateToday)
//        dateAlerAddTask.setText("Date  $formatted")
//
//        var count = AppRepo.nextIdList
//        ed_taksTitle = myView.findViewById(R.id.edtTitleTask)
//        ed_taskDescription = myView.findViewById(R.id.edtDescription)
//        calendarTask = myView.findViewById(R.id.id_calendar)
//
//
//        calendarTask.setOnClickListener {
//            val datePickerDialog = DatePickerDialog(
//                requireView().context,
//                DatePickerDialog.OnDateSetListener { view, y, m, d ->
//                    dueDate = "$y/${m + 1}/$d"
//                    calendarTask.setText(dueDate)
//                },
//                year,
//                month,
//                day
//            )
//            datePickerDialog.datePicker.minDate = c.timeInMillis
//            datePickerDialog.show()
//
//        }
//
//
//
//
//        Save.setOnClickListener {
//
//            if (ed_taksTitle.text.isNotEmpty()
//                && calendarTask.text.isNotEmpty()
//            ) {
//                //delete...
//                Toast.makeText(
//                    context,
//                    " Title task : ${ed_taksTitle.text}" + " \n Description ${ed_taskDescription.text} \n" + " $formatted \n Due Date $dueDate",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                //Insert to list var insertTask
//                insertTask = DataTask(
//                    count,
//                    "${ed_taksTitle.text}",
//                    "${ed_taskDescription.text}",
//                    "$formatted",
//                    "$dueDate",
//                    false
//                )
//
//                //mainViewModel.insertTask(insertTask)
//
//
//                //to insert to database
//                insertDateToDatabase(
//                    mainViewModel,
//                    ed_taksTitle.text.toString(),
//                    ed_taskDescription.text.toString()
//                )
//
//
//
//                mainViewModel.getAllTasks()
//
//
//
//
//                //رتب الكود
//                mainViewModel.getAllTasks().observe(viewLifecycleOwner,  {
//                    rv_recyclerView.adapter=RecyclerAdapter(it,mainViewModel)})
//                //search about notifyDataSetChanged
//                rv_recyclerView.adapter?.notifyDataSetChanged()
//
//                //fun Clear
//                clearEditText()
//                count++
//            } else {
//                Toast.makeText(context, " Please Complete  ", Toast.LENGTH_SHORT).show()
//            }
//
//
//
//
//
//
//
//        }
//
//        addTask.setNegativeButton(
//            "Cancel",
//            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
//        addTask.show()//.window?.setBackgroundDrawableResource(R.drawable.ic_launcher_foreground)
//
//    }
}