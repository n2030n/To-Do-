package com.hani.todo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ToDoAdapter(context: Context, toDoList: MutableList<ToDoModel>) : BaseAdapter() {

    // Creating the current date and store it in creationDate
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val creationDateFormatted = current.format(formatter)


    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var itemList = toDoList
    private var updateAndDelete: UpdateAndDelete = context as UpdateAndDelete

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(p0: Int): Any {
        return itemList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
        val UID: String = itemList.get(p0).UID as String
        val taskTitle = itemList.get(p0).taskTitle as String
        val creationDate = itemList.get(p0).creationDate as String
        val dueDate: String = itemList.get(p0).dueDate as String
        val done: Boolean = itemList.get(p0).done as Boolean

        val view: View
        val viewHolder: ListViewHolder

        if (p1 == null) {
            view = inflater.inflate(R.layout.row_itemlayout, p2, false)
            viewHolder = ListViewHolder(view)
            view.tag = viewHolder
        } else {
            view = p1
            viewHolder = view.tag as ListViewHolder
        }
        viewHolder.taskTitle.text = taskTitle
        viewHolder.creationDate.text = creationDate
        viewHolder.dueDate.text = dueDate
        viewHolder.isDone.isChecked = done

        if (done) {
            viewHolder.cardViewXML.setBackgroundColor(Color.GRAY)
        } else {
            if (dueDate < creationDateFormatted) {
                viewHolder.cardViewXML.setBackgroundColor(Color.RED)
                viewHolder.isDone.isEnabled = false
            } else {
                viewHolder.cardViewXML.setBackgroundColor(Color.WHITE)
            }
        }


        viewHolder.isDone.setOnClickListener {
            updateAndDelete.modefyItem(UID, !done)
        }

        viewHolder.isDeleted.setOnClickListener {
            updateAndDelete.onItemDelete(UID)
        }
        return view
    }

    private class ListViewHolder(row: View?) {
        val taskTitle: TextView = row!!.findViewById(R.id.item_textView) as TextView
        val creationDate: TextView = row!!.findViewById(R.id.creationDate) as TextView
        val dueDate: TextView = row!!.findViewById(R.id.dueDate) as TextView
        val isDone: CheckBox = row!!.findViewById(R.id.checkBox) as CheckBox
        val isDeleted: ImageButton = row!!.findViewById(R.id.close) as ImageButton
        val cardViewXML: ConstraintLayout = row!!.findViewById(R.id.relativeLayout) as ConstraintLayout

    }
}

