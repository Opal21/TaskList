package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.lang.reflect.ParameterizedType

data class Task(var index: Int, var priority: Char, var deadDate: String, var deadTime: String, var dueTag: Char, var description: String)


class TaskList {
    private var taskList = mutableListOf<Task>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
    private val taskListAdapter = moshi.adapter<MutableList<Task>>(type)

    private fun setPriority(): Char {
        var priority = 'N'
        do {
            println("Input the task priority (C, H, N, L):")
            val usrInput = readln().uppercase()
            priority = when (usrInput) {
                "C" -> 'C'
                "H" -> 'H'
                "N" -> 'N'
                "L" -> 'L'
                else -> continue
            }
        } while (!usrInput.matches(Regex("[CHNL]")))
        return priority
    }

    private fun setDeadDate(): String {
        var deadDate = ""
        do {
            println("Input the date (yyyy-mm-dd):")
            val date = readln()
            val validDate = dateExists(date)
            if (validDate == null) {
                println("The input date is invalid")
            } else
                deadDate = validDate.toString()
        } while (validDate == null)
        return deadDate
    }

    private fun dateExists(maybeDate: String): LocalDate? {
        val dateSplitString = maybeDate.split("-")
        if (dateSplitString.size != 3) { return null }
        val dateSplitInt = IntArray(3)
        for (i in dateSplitInt.indices) {
            dateSplitInt[i] = Integer.parseInt(dateSplitString[i])
        }
        return try {
            LocalDate(dateSplitInt[0], dateSplitInt[1], dateSplitInt[2])
        } catch (e: IllegalArgumentException){
            null
        }
    }

    private fun setDeadTime(): String {
        var deadTime = ""
        val timeRegex = "([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-9]|[0-5][0-9])\$".toRegex()
        do {
            println("Input the time (hh:mm):")
            val time = readln()
            if (time.matches(timeRegex)) {
                deadTime = time
            } else {
                println("The input time is invalid")
            }
        } while (!time.matches(timeRegex))
        return if (deadTime == "0:0") {
            "00:00"
        } else
            deadTime
    }

    private fun setDueTag(deadline: String): Char {
        val dateSplitString = deadline.split("-")
        val dateSplitInt = IntArray(3)
        for (i in dateSplitInt.indices) {
            dateSplitInt[i] = Integer.parseInt(dateSplitString[i])
        }
        val taskDate = LocalDate(dateSplitInt[0], dateSplitInt[1], dateSplitInt[2])
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        return when {
            numberOfDays > 0 -> 'I'
            numberOfDays < 0 -> 'O'
            else -> 'T'
        }
    }

    private fun setDescription(): String? {
        var description = ""
        println("Input a new task (enter a blank line to end):")
        do {
            val line = readln().trim()
            if (line.isNotBlank())
            {
                description += "$line\n"
            }
        } while (line.isNotBlank())
        return description.ifEmpty {
            println("The task is blank")
            null
        }
    }

    private fun addTask() {
        val newIndex = taskList.size
        val priority = setPriority()
        val deadDate = setDeadDate()
        val deadTime = setDeadTime()
        val dueTag = setDueTag(deadDate)
        val description = setDescription()
        if (description == null) {
            return
        } else {
            val newTask = Task(newIndex, priority, deadDate, deadTime, dueTag, description)
            taskList.add(newTask)
        }
    }

    private fun printDescription(description: String) {
        var charNum = 0
        for (char in description.trim()) {
            if (charNum == 44) {
                charNum = 0
                print("|\n|    |            |       |   |   |")
            }
            if (char == '\n') {
                repeat(44-charNum) { print(" ") }
                print("|\n|    |            |       |   |   |")
                charNum = 0
            } else {
                print(char)
                charNum += 1
            }
        }
        repeat(44 - charNum) { print(" ") }
        println("|")
    }

    private fun printTasks() {
        if(taskList.isEmpty()) {
            println("No tasks have been input")
        } else {
            print("""+----+------------+-------+---+---+--------------------------------------------+
| N  |    Date    | Time  | P | D |                   Task                     |
+----+------------+-------+---+---+--------------------------------------------+""")
            println()
            for (task in taskList) {
                if (task.index < 9) {
                    print("| ${task.index + 1}  | ${task.deadDate} | ${task.deadTime} |")
                    when (task.priority) {
                        'C' -> print(" \u001B[101m \u001B[0m |")
                        'H' -> print(" \u001B[103m \u001B[0m |")
                        'N' -> print(" \u001B[102m \u001B[0m |")
                        'L' -> print(" \u001B[104m \u001B[0m |")
                    }
                    when (task.dueTag) {
                        'I' -> print(" \u001B[102m \u001B[0m |")
                        'T' -> print(" \u001B[103m \u001B[0m |")
                        'O' -> print(" \u001B[101m \u001B[0m |")
                    }
                } else {
                    print("| ${task.index + 1} | ${task.deadDate} | ${task.deadTime} |")
                    when (task.priority) {
                        'C' -> print(" \u001B[101m \u001B[0m |")
                        'H' -> print(" \u001B[103m \u001B[0m |")
                        'N' -> print(" \u001B[102m \u001B[0m |")
                        'L' -> print(" \u001B[104m \u001B[0m |")
                    }
                    when (task.dueTag) {
                        'I' -> print(" \u001B[102m \u001B[0m |")
                        'T' -> print(" \u001B[103m \u001B[0m |")
                        'O' -> print(" \u001B[101m \u001B[0m |")
                    }
                }
                printDescription(task.description)
                println("+----+------------+-------+---+---+--------------------------------------------+")
            }
        }
    }

    private fun editTask() {
        val index = selectTask()
        if (index == 0) {
            return
        }
        val taskToEdit = taskList[index-1]
        val possibleActions = arrayOf("priority", "date", "time", "task")
        do {
            println("Input a field to edit (priority, date, time, task):")
            val action = readln()
            when (action) {
                "priority" -> {
                    val newPriority = setPriority()
                    taskToEdit.priority = newPriority
                }
                "date" -> {
                    val newDeadDate = setDeadDate()
                    taskToEdit.deadDate = newDeadDate
                    setDueTag(newDeadDate)
                }
                "time" -> {
                    val newDeadTime = setDeadTime()
                    taskToEdit.deadTime = newDeadTime
                }
                "task" -> {
                    val newDescription = setDescription()
                    if (newDescription != null) {
                        taskToEdit.description = newDescription
                    }
                }
                else -> println("Invalid field")
            }
        } while (action !in possibleActions)
        println("The task is changed")
    }

    private fun deleteTask() {
        val index = selectTask()
        if (index == 0) {
            return
        }
        taskList.removeAt(index - 1)
        println("The task is deleted")
        for (task in taskList) {
            task.index = taskList.indexOf(task)
        }
    }

    private fun selectTask(): Int {
        printTasks()
        if(taskList.isEmpty()) {
            return 0
        }
        var number = 0
        do {
            println("Input the task number (1-${taskList.lastIndex + 1}):")
            try {
                number = Integer.parseInt(readln())
            } catch (e: Exception) {
                println("Invalid task number")
                continue
            }
            if (number !in 1..taskList.lastIndex + 1)
                println("Invalid task number")
        } while (number !in 1..taskList.lastIndex + 1)
        return number
    }

    private fun readTaskList() {
        val jsonFile = File("tasklist.json")
        taskList = taskListAdapter.fromJson(jsonFile.readText())!!
    }

    private fun saveTaskList() {
        val jsonFile = File("tasklist.json")
        jsonFile.writeText(taskListAdapter.toJson(taskList))
    }

    fun run() {
        do {
            if (File("tasklist.json").exists()) { readTaskList() }
            println("Input an action (add, print, edit, delete, end):")
            when(readln()) {
                "add" -> addTask()
                "print" -> printTasks()
                "edit" -> editTask()
                "delete" -> deleteTask()
                "end" -> {
                    println("Tasklist exiting!")
                    saveTaskList()
                    break
                }
                else -> println("The input action is invalid")
            }
        } while (true)
    }
}

fun main() {
    val taskList = TaskList()
    taskList.run()
}
