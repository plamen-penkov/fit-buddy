package com.example.my_fitness_pal_clone.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.GestureDetectorCompat
import com.example.my_fitness_pal_clone.utils.DataManager
import com.example.my_fitness_pal_clone.utils.FoodItem
import com.example.my_fitness_pal_clone.utils.setupBottomNavigation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Suppress("DEPRECATION")
class DiaryActivity : AppCompatActivity() {

    private lateinit var diaryContent: LinearLayout
    private val mealCategories = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
    private var displayedDateString: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private lateinit var gestureDetector: GestureDetectorCompat
    private var isAnimating = false

    private val colorPrimary = "#0066EE".toColorInt()
    private val colorBackground = "#F5F7FA".toColorInt()
    private val colorCard = Color.WHITE
    private val colorTextPrimary = "#111827".toColorInt()
    private val colorTextSecondary = "#6B7280".toColorInt()
    private val colorDivider = "#E5E7EB".toColorInt()
    private val colorFoodRowBg = "#F9FAFB".toColorInt()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataManager.loadData(this)

        displayedDateString = DataManager.getCurrentDate()
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        val root = RelativeLayout(this).apply {
            setBackgroundColor(colorBackground)
        }

        val scrollView = ScrollView(this).apply {
            id = View.generateViewId()
            isScrollbarFadingEnabled = false
        }

        diaryContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 30.dp(), 20.dp(), 30.dp())
        }
        scrollView.addView(diaryContent)

        val scrollParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            bottomMargin = 80.dp()
        }
        root.addView(scrollView, scrollParams)

        refreshDiary()
        setupBottomNavigation(this, root, 2)
        setContentView(root)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun changeDate(offsetDays: Int) {
        if (isAnimating) return
        isAnimating = true

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val outAnimX = if (offsetDays > 0) -screenWidth else screenWidth
        val inAnimX = if (offsetDays > 0) screenWidth else -screenWidth

        diaryContent.animate()
            .translationX(outAnimX)
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(displayedDateString) ?: Date()
                calendar.add(Calendar.DAY_OF_YEAR, offsetDays)

                displayedDateString = dateFormat.format(calendar.time)
                refreshDiary()

                diaryContent.translationX = inAnimX
                diaryContent.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(150)
                    .withEndAction { isAnimating = false }
                    .start()
            }
            .start()
    }

    private fun refreshDiary() {
        diaryContent.removeAllViews()
        val currentFoods = DataManager.consumedFoods.filter { it.date == displayedDateString }

        diaryContent.addView(createHeaderTitle())
        diaryContent.addView(createSummaryCard(currentFoods))

        mealCategories.forEach { meal ->
            val mealFoods = currentFoods.filter { it.mealType == meal }
            diaryContent.addView(createMealSection(meal, mealFoods))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createHeaderTitle(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }
        }

        container.addView(TextView(this).apply {
            text = "Diary"
            textSize = 28f
            setTextColor(colorTextPrimary)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 10.dp())
        })

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            background = createRoundedBackground(colorCard, 12.dp().toFloat())
            elevation = 4.dp().toFloat()
            setPadding(10.dp(), 10.dp(), 10.dp(), 10.dp())
        }

        navRow.addView(TextView(this).apply {
            text = "◀"
            textSize = 18f
            setTextColor(colorPrimary)
            setPadding(15.dp(), 10.dp(), 20.dp(), 10.dp())
            setOnClickListener { changeDate(-1) }
        })

        navRow.addView(TextView(this).apply {
            text = getFriendlyDateText(displayedDateString)
            textSize = 16f
            setTextColor(colorTextPrimary)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        })

        navRow.addView(TextView(this).apply {
            text = "▶"
            textSize = 18f
            setTextColor(colorPrimary)
            setPadding(20.dp(), 10.dp(), 15.dp(), 10.dp())
            setOnClickListener { changeDate(1) }
        })

        container.addView(navRow)
        return container
    }

    @SuppressLint("SetTextI18n")
    private fun createSummaryCard(foods: List<FoodItem>): View {
        val totalCals = foods.sumOf { it.calories }
        val totalP = foods.sumOf { it.protein }
        val totalC = foods.sumOf { it.carbs }
        val totalF = foods.sumOf { it.fats }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 20.dp(), 20.dp(), 20.dp())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 25.dp())
            }
            background = createRoundedBackground(colorCard, 15.dp().toFloat())
            elevation = 6.dp().toFloat()
        }

        card.addView(TextView(this).apply {
            text = "Daily Summary"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
        })

        card.addView(TextView(this).apply {
            text = "$totalCals kcal"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorPrimary)
            setPadding(0, 5.dp(), 0, 15.dp())
        })

        // IMPLEMENTING TABLE LAYOUT HERE
        val table = TableLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            isStretchAllColumns = true
        }

        val headerRow = TableRow(this)
        headerRow.addView(createTableText("Protein", true))
        headerRow.addView(createTableText("Carbs", true))
        headerRow.addView(createTableText("Fats", true))

        val valueRow = TableRow(this)
        valueRow.addView(createTableText("${totalP}g", false))
        valueRow.addView(createTableText("${totalC}g", false))
        valueRow.addView(createTableText("${totalF}g", false))

        table.addView(headerRow)
        table.addView(valueRow)
        card.addView(table)

        return card
    }

    private fun createTableText(str: String, isHeader: Boolean): TextView {
        return TextView(this).apply {
            text = str
            textSize = if (isHeader) 12f else 16f
            setTextColor(if (isHeader) colorTextSecondary else colorTextPrimary)
            setTypeface(null, if (isHeader) Typeface.NORMAL else Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 4.dp(), 0, 4.dp())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createMealSection(mealName: String, foods: List<FoodItem>): View {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 20.dp(), 20.dp(), 20.dp())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }
            background = createRoundedBackground(colorCard, 12.dp().toFloat())
            elevation = 4.dp().toFloat()
        }

        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }

        headerLayout.addView(TextView(this).apply {
            text = mealName
            textSize = 20f
            setTextColor(colorTextPrimary)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        })

        headerLayout.addView(TextView(this).apply {
            text = "${foods.sumOf { it.calories }} kcal"
            textSize = 16f
            setTextColor(colorTextSecondary)
            setTypeface(null, Typeface.BOLD)
        })

        section.addView(headerLayout)
        section.addView(createDivider())

        if (foods.isEmpty()) {
            section.addView(TextView(this).apply {
                text = "No food logged yet."
                setTextColor(colorTextSecondary)
                setPadding(0, 10.dp(), 0, 10.dp())
                textSize = 15f
            })
        } else {
            foods.forEach { section.addView(createFoodRow(it)) }
        }

        section.addView(TextView(this).apply {
            text = "+ Add Food"
            textSize = 16f
            setTextColor(colorPrimary)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 12.dp(), 0, 2.dp())
            setOnClickListener { showAddFoodDialog(mealName) }
        })

        return section
    }

    @SuppressLint("SetTextI18n")
    private fun createFoodRow(food: FoodItem): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 8.dp(), 0, 8.dp())
            }
            background = createRoundedBackground(colorFoodRowBg, 10.dp().toFloat())
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { showDeleteDialog(food) }

            val textLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                addView(TextView(context).apply {
                    text = food.name
                    textSize = 16f
                    setTextColor(colorTextPrimary)
                    setTypeface(null, Typeface.BOLD)
                })
                addView(TextView(context).apply {
                    text = "P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fats}g"
                    textSize = 13f
                    setTextColor(colorTextSecondary)
                    setPadding(0, 4.dp(), 0, 0)
                })
            }
            addView(textLayout)

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
                addView(TextView(context).apply {
                    text = "${food.calories}"
                    textSize = 18f
                    setTextColor(colorPrimary)
                    setTypeface(null, Typeface.BOLD)
                })
                addView(TextView(context).apply {
                    text = "kcal"; textSize = 12f; setTextColor(colorTextSecondary)
                })
            })
        }
    }

    private fun getFriendlyDateText(dateStr: String): String {
        if (dateStr == DataManager.getCurrentDate()) return "Today"
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(dateStr) ?: Date()

        val other = Calendar.getInstance()

        other.add(Calendar.DAY_OF_YEAR, -1)
        if (isSameDay(calendar, other)) return "Yesterday"

        other.add(Calendar.DAY_OF_YEAR, 2) // Move from -1 to +1
        if (isSameDay(calendar, other)) return "Tomorrow"

        return displayFormat.format(calendar.time)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showDeleteDialog(food: FoodItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Remove ${food.name}?")
            .setPositiveButton("Delete") { _, _ ->
                DataManager.removeFood(this, food)
                refreshDiary()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showAddFoodDialog(preselectedMeal: String) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30.dp(), 30.dp(), 30.dp(), 10.dp())
        }

        dialogLayout.addView(TextView(this).apply {
            text = "Log Food"; textSize = 22f; setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary); setPadding(0, 0, 0, 15.dp())
        })

        fun createInput(hintText: String, isNumber: Boolean = false): EditText {
            return EditText(this@DiaryActivity).apply {
                hint = hintText
                if (isNumber) inputType = InputType.TYPE_CLASS_NUMBER
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    setMargins(0, 8.dp(), 0, 8.dp())
                }
                background = createRoundedBackground(colorFoodRowBg, 8.dp().toFloat()).apply {
                    setStroke(1.dp(), colorDivider)
                }
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                setTextColor(colorTextPrimary); setHintTextColor(colorTextSecondary); textSize = 16f
            }
        }

        val nameInput = createInput("Food Name")
        val proteinInput = createInput("Protein (g)", true)
        val carbsInput = createInput("Carbs (g)", true)
        val fatsInput = createInput("Fats (g)", true)

        dialogLayout.addAllViews(nameInput, proteinInput, carbsInput, fatsInput)

        val mealDropdown = Spinner(this).apply {
            adapter = ArrayAdapter(this@DiaryActivity, android.R.layout.simple_spinner_dropdown_item, mealCategories)
            setSelection(mealCategories.indexOf(preselectedMeal))
            background = createRoundedBackground(colorFoodRowBg, 8.dp().toFloat()).apply { setStroke(1.dp(), colorDivider) }
            setPadding(10.dp(), 5.dp(), 10.dp(), 5.dp())
        }
        dialogLayout.addView(mealDropdown)

        AlertDialog.Builder(this).setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString()
                if (name.isNotEmpty()) {
                    DataManager.addFood(this, FoodItem(name,
                        proteinInput.text.toString().toIntOrNull() ?: 0,
                        carbsInput.text.toString().toIntOrNull() ?: 0,
                        fatsInput.text.toString().toIntOrNull() ?: 0,
                        displayedDateString, mealDropdown.selectedItem.toString()))
                    refreshDiary()
                }
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun View.addAllViews(vararg views: View) = views.forEach { (this as LinearLayout).addView(it) }

    private fun createDivider() = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 1.dp()).apply { setMargins(0, 10.dp(), 0, 10.dp()) }
        setBackgroundColor(colorDivider)
    }

    private fun createRoundedBackground(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color); cornerRadius = radius
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100

        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent,
            velocityX: Float, velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            // Ensure the swipe is mostly horizontal
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        // Swipe Right (Go to Previous Day)
                        changeDate(-1)
                    } else {
                        // Swipe Left (Go to Next Day)
                        changeDate(1)
                    }
                    return true
                }
            }
            return false
        }
    }
}