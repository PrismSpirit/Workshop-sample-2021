package page.chungjungsoo.to_dosample.todo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )
        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDescription)
        val dateandtime : TextView = view.findViewById(R.id.listDateandTime)
        val checker : TextView = view.findViewById(R.id.listchecker)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)

        db = TodoDatabaseHelper(this.context)

        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.description
        dateandtime.text = todo.date

        if (todo.finished) {
            checker.text = "Finished"
        }
        else {
            checker.text = "Not Finished"
        }

        // OnClick Listener for edit button on every ListView items
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            titleToAdd.setText(todo.title)
            descriptionToAdd.setText(todo.description)

            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            val dateToAdd = dialogView.findViewById<Button>(R.id.todoDuedate_date)
            val timeToAdd = dialogView.findViewById<Button>(R.id.todoDuedate_time)
            val dateDisplay = dialogView.findViewById<TextView>(R.id.duedate_date)
            val timeDisplay = dialogView.findViewById<TextView>(R.id.duedate_time)
            dateDisplay.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
            timeDisplay.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("H시 m분"))

            val finishedchecker = dialogView.findViewById<CheckBox>(R.id.checkbox_isfinished)

            dateToAdd.setOnClickListener {
                var calendar = Calendar.getInstance()
                var year = calendar.get(Calendar.YEAR)
                var month = calendar.get(Calendar.MONTH)
                var day = calendar.get(Calendar.DAY_OF_MONTH)

                var listener = DatePickerDialog.OnDateSetListener { _, i, i2, i3 ->
                    dateDisplay.text = "${i}년 ${i2 + 1}월 ${i3}일"
                }

                var dpicker = DatePickerDialog(this.context, listener, year, month, day)
                dpicker.show()
            }

            timeToAdd.setOnClickListener {
                var calendar = Calendar.getInstance()
                var hour = calendar.get(Calendar.HOUR)
                var minute = calendar.get(Calendar.MINUTE)

                var listener = TimePickerDialog.OnTimeSetListener { _, i, i2 ->
                    timeDisplay.text = "${i}시 ${i2}분"
                }

                // boolean is24HourView : true일 때 24시간으로 표기
                var tpicker = TimePickerDialog(this.context, listener, hour, minute, false)

                tpicker.show()
            }


            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    var isfinished = finishedchecker.isChecked
                    val tmp = Todo(
                        titleToAdd.text.toString(),
                        descriptionToAdd.text.toString(),
                        dateDisplay.text.toString(),
                        timeDisplay.text.toString(),
                        isfinished
                    )

                    val result = db.updateTodo(tmp, position)
                    if (result) {
                        todo.title = titleToAdd.text.toString()
                        todo.description = descriptionToAdd.text.toString()
                        todo.date = dateDisplay.text.toString()
                        todo.time = timeDisplay.text.toString()
                        todo.finished = isfinished
                        notifyDataSetChanged()
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
            val result = db.delTodo(position)
            if (result) {
                items.removeAt(position)
                notifyDataSetChanged()
            }
            else {
                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }
        }

        return view
    }
}