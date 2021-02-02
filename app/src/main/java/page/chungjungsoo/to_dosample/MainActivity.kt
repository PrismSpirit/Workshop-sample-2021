package page.chungjungsoo.to_dosample

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import page.chungjungsoo.to_dosample.todo.Todo
import page.chungjungsoo.to_dosample.todo.TodoDatabaseHelper
import page.chungjungsoo.to_dosample.todo.TodoListViewAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    var dbHandler : TodoDatabaseHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view - loads activity_main.xml
        setContentView(R.layout.activity_main)

        // Set app status bar color : white, force light status bar mode
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Set light status bar mode depending on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add database helper and load data from database
        dbHandler = TodoDatabaseHelper(this)
        var todolist: MutableList<Todo> = dbHandler!!.getAll()

        // Put data with custom listview adapter
        todoList.adapter = TodoListViewAdapter(this, R.layout.todo_item, todolist)
        todoList.emptyView = helpText

        // Onclick listener for add button
        addBtn.setOnClickListener {
            // By pressing the add button, we will inflate an AlertDialog.
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)

            // Get elements from custom dialog layout (add_todo_dialog.xml)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)

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
                    dateDisplay.text = "${i}년 ${i2}월 ${i3}일"
                }

                var dpicker = DatePickerDialog(this, listener, year, month, day)
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
                var tpicker = TimePickerDialog(this, listener, hour, minute, false)

                tpicker.show()
            }

            // Add InputMethodManager for auto keyboard popup
            val ime = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Cursor auto focus on title when AlertDialog is inflated
            titleToAdd.requestFocus()

            // Show keyboard when AlertDialog is inflated
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            // Add positive button and negative button for AlertDialog.
            // Pressing the positive button: Add data to the database and also add them in listview and update.
            // Pressing the negative button: Do nothing. Close the AlertDialog
            val add = builder.setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    if (!TextUtils.isEmpty(titleToAdd.text.trim())) {
                        // Add item to the database
                        var isfinished = finishedchecker.isChecked
                        val todo = Todo(
                            titleToAdd.text.toString(),
                            descriptionToAdd.text.toString(),
                            dateDisplay.text.toString(),
                            timeDisplay.text.toString(),
                            isfinished
                        )
                        dbHandler!!.addTodo(todo)

                        // Add them to listview and update.
                        (todoList.adapter as TodoListViewAdapter).add(todo)
                        (todoList.adapter as TodoListViewAdapter).notifyDataSetChanged()

                        // Close keyboard
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this,
                            "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE)

            // Default status of add button should be disabled. Because when AlertDialog inflates,
            // the title is empty by default and we do not want empty titles to be added to listview
            // and in databases.
            add.isEnabled = false

            // Listener for title text. If something is inputted in title, we should re-enable the add button.
            titleToAdd.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (!TextUtils.isEmpty(p0.toString().trim())) {
                        add.isEnabled = true
                    }
                    else {
                        titleToAdd.error = "TODO 제목을 입력하세요!"
                        add.isEnabled = false
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
        }
    }
}