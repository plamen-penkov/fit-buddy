package com.example.fit_buddy.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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
import androidx.core.view.GestureDetectorCompat
import com.example.fit_buddy.R
import com.example.fit_buddy.data.FoodItemEntity
import com.example.fit_buddy.utils.DataManager
import com.example.fit_buddy.utils.setupBottomNavigation
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs

@Suppress("DEPRECATION")
class DiaryActivity : AppCompatActivity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var diaryContent: LinearLayout
    private val mealCategories = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
    private var displayedDateString: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private lateinit var gestureDetector: GestureDetectorCompat
    private var isAnimating = false

    private val colorPrimary by lazy { getColor(R.color.app_primary) }
    private val colorBackground by lazy { getColor(R.color.app_background) }
    private val colorCard by lazy { getColor(R.color.app_surface) }
    private val colorTextPrimary by lazy { getColor(R.color.app_on_surface) }
    private val colorTextSecondary by lazy { getColor(R.color.app_on_surface_variant) }
    private val colorDivider by lazy { getColor(R.color.app_outline) }
    private val colorFoodRowBg by lazy { getColor(R.color.app_surface_variant) }
    private val colorError by lazy { getColor(R.color.app_error) }

    private val executor = Executors.newSingleThreadExecutor()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayedDateString = DataManager.getCurrentDate()
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        rootLayout = RelativeLayout(this).apply {
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
        rootLayout.addView(scrollView, scrollParams)

        executor.execute {
            DataManager.loadData(this)
            runOnUiThread { refreshDiary() }
        }

        setupBottomNavigation(this, rootLayout, 2)
        setContentView(rootLayout)
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
    private fun createSummaryCard(foods: List<FoodItemEntity>): View {
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
    private fun createMealSection(mealName: String, foods: List<FoodItemEntity>): View {
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
            foods.forEach { section.addView(createSwipeableFoodRow(it, section)) }
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

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun createSwipeableFoodRow(food: FoodItemEntity, parentSection: LinearLayout): View {
        val rowHeight = 80.dp()

        // Container that holds both background and foreground
        val container = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 8.dp(), 0, 8.dp())
            }
            clipChildren = false
        }

        // Red delete background
        val deleteBackground = LinearLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            background = createRoundedBackground(colorError, 10.dp().toFloat())
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, 0, 20.dp(), 0)

            addView(TextView(this@DiaryActivity).apply {
                text = "🗑 Delete"
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, Typeface.BOLD)
            })
        }
        container.addView(deleteBackground)

        // Foreground: the actual food row content
        val foreground = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            background = createRoundedBackground(colorFoodRowBg, 10.dp().toFloat())
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
            gravity = Gravity.CENTER_VERTICAL
            // Tap to edit
            setOnClickListener { showEditFoodDialog(food) }
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            addView(TextView(this@DiaryActivity).apply {
                text = food.name
                textSize = 16f
                setTextColor(colorTextPrimary)
                setTypeface(null, Typeface.BOLD)
            })
            addView(TextView(this@DiaryActivity).apply {
                text = "P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fats}g"
                textSize = 13f
                setTextColor(colorTextSecondary)
                setPadding(0, 2.dp(), 0, 0)
            })
            addView(TextView(this@DiaryActivity).apply {
                val servingsText = if (food.servings == food.servings.toInt().toDouble())
                    "${food.servings.toInt()} × 100g"
                else
                    String.format(Locale.getDefault(), "%.1f × 100g", food.servings)
                text = servingsText
                textSize = 11f
                setTextColor(colorTextSecondary)
                setPadding(0, 2.dp(), 0, 0)
            })
        }
        foreground.addView(textLayout)

        foreground.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            addView(TextView(this@DiaryActivity).apply {
                text = "${food.calories}"
                textSize = 18f
                setTextColor(colorPrimary)
                setTypeface(null, Typeface.BOLD)
            })
            addView(TextView(this@DiaryActivity).apply {
                text = "kcal"; textSize = 12f; setTextColor(colorTextSecondary)
            })
        })

        container.addView(foreground)

        // Swipe-to-delete gesture
        var startX = 0f
        val swipeThreshold = 150.dp().toFloat()

        foreground.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    false // Let click listener still work
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - startX
                    if (deltaX < 0) { // Only allow left swipe
                        foreground.translationX = deltaX
                        // If swiped enough, consume the event
                        if (abs(deltaX) > 20.dp()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    abs(deltaX) > 20.dp()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.rawX - startX
                    if (deltaX < -swipeThreshold) {
                        // Swiped far enough — delete with animation
                        foreground.animate()
                            .translationX(-foreground.width.toFloat())
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction {
                                performDelete(food)
                            }
                            .start()
                    } else {
                        // Snap back
                        foreground.animate()
                            .translationX(0f)
                            .setDuration(150)
                            .start()
                    }
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    abs(deltaX) > 20.dp()
                }
                else -> false
            }
        }

        return container
    }

    private fun performDelete(food: FoodItemEntity) {
        executor.execute {
            DataManager.removeFood(this, food)
            runOnUiThread {
                refreshDiary()
                Snackbar.make(rootLayout, "${food.name} deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        executor.execute {
                            DataManager.addFood(this, food)
                            runOnUiThread { refreshDiary() }
                        }
                    }
                    .show()
            }
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

    @SuppressLint("SetTextI18n")
    private fun showAddFoodDialog(preselectedMeal: String) {
        showFoodFormDialog(
            title = "Log Food",
            existingFood = null,
            preselectedMeal = preselectedMeal
        )
    }

    @SuppressLint("SetTextI18n")
    private fun showEditFoodDialog(food: FoodItemEntity) {
        showFoodFormDialog(
            title = "Edit Food",
            existingFood = food,
            preselectedMeal = food.mealType
        )
    }

    @SuppressLint("SetTextI18n")
    private fun showFoodFormDialog(title: String, existingFood: FoodItemEntity?, preselectedMeal: String) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30.dp(), 30.dp(), 30.dp(), 10.dp())
        }

        dialogLayout.addView(TextView(this).apply {
            text = title; textSize = 22f; setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary); setPadding(0, 0, 0, 15.dp())
        })

        // Hint: values are per 100g
        dialogLayout.addView(TextView(this).apply {
            text = "Enter macros per 100g of food"
            textSize = 13f
            setTextColor(colorTextSecondary)
            setPadding(0, 0, 0, 10.dp())
        })

        fun createInput(hintText: String, isNumber: Boolean = false, isDecimal: Boolean = false): EditText {
            return EditText(this@DiaryActivity).apply {
                hint = hintText
                if (isNumber) inputType = InputType.TYPE_CLASS_NUMBER
                if (isDecimal) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
        val proteinInput = createInput("Protein per 100g (g)", true)
        val carbsInput = createInput("Carbs per 100g (g)", true)
        val fatsInput = createInput("Fats per 100g (g)", true)
        val servingsInput = createInput("Servings (× 100g)", isDecimal = true)

        // Pre-fill for edit
        if (existingFood != null) {
            nameInput.setText(existingFood.name)
            proteinInput.setText(existingFood.proteinPer100g.toString())
            carbsInput.setText(existingFood.carbsPer100g.toString())
            fatsInput.setText(existingFood.fatsPer100g.toString())
            servingsInput.setText(
                if (existingFood.servings == existingFood.servings.toInt().toDouble())
                    existingFood.servings.toInt().toString()
                else
                    existingFood.servings.toString()
            )
        } else {
            servingsInput.setText("1")
        }

        dialogLayout.addAllViews(nameInput, proteinInput, carbsInput, fatsInput, servingsInput)

        // Live preview of calculated totals
        val previewText = TextView(this).apply {
            text = "Total: 0 kcal (P: 0g • C: 0g • F: 0g)"
            textSize = 13f
            setTextColor(colorPrimary)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8.dp(), 0, 12.dp())
        }
        dialogLayout.addView(previewText)

        // Update preview on any input change
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val p = proteinInput.text.toString().toIntOrNull() ?: 0
                val c = carbsInput.text.toString().toIntOrNull() ?: 0
                val f = fatsInput.text.toString().toIntOrNull() ?: 0
                val srv = servingsInput.text.toString().toDoubleOrNull() ?: 1.0
                val totalP = (p * srv).toInt()
                val totalC = (c * srv).toInt()
                val totalF = (f * srv).toInt()
                val totalCal = (totalP * 4) + (totalC * 4) + (totalF * 9)
                previewText.text = "Total: $totalCal kcal (P: ${totalP}g • C: ${totalC}g • F: ${totalF}g)"
            }
        }
        proteinInput.addTextChangedListener(watcher)
        carbsInput.addTextChangedListener(watcher)
        fatsInput.addTextChangedListener(watcher)
        servingsInput.addTextChangedListener(watcher)
        // Trigger initial preview
        watcher.afterTextChanged(null)

        val mealDropdown = Spinner(this).apply {
            adapter = ArrayAdapter(this@DiaryActivity, android.R.layout.simple_spinner_dropdown_item, mealCategories)
            setSelection(mealCategories.indexOf(preselectedMeal))
            background = createRoundedBackground(colorFoodRowBg, 8.dp().toFloat()).apply { setStroke(1.dp(), colorDivider) }
            setPadding(10.dp(), 5.dp(), 10.dp(), 5.dp())
        }
        dialogLayout.addView(mealDropdown)

        val buttonText = if (existingFood != null) "Update" else "Save"

        AlertDialog.Builder(this).setView(dialogLayout)
            .setPositiveButton(buttonText) { _, _ ->
                val name = nameInput.text.toString()
                if (name.isNotEmpty()) {
                    val p100 = proteinInput.text.toString().toIntOrNull() ?: 0
                    val c100 = carbsInput.text.toString().toIntOrNull() ?: 0
                    val f100 = fatsInput.text.toString().toIntOrNull() ?: 0
                    val srv = servingsInput.text.toString().toDoubleOrNull() ?: 1.0

                    executor.execute {
                        if (existingFood != null) {
                            val updated = existingFood.copy(
                                name = name,
                                proteinPer100g = p100,
                                carbsPer100g = c100,
                                fatsPer100g = f100,
                                servings = srv,
                                mealType = mealDropdown.selectedItem.toString()
                            )
                            DataManager.updateFood(this, updated)
                        } else {
                            DataManager.addFood(this, FoodItemEntity(
                                name = name,
                                proteinPer100g = p100,
                                carbsPer100g = c100,
                                fatsPer100g = f100,
                                servings = srv,
                                date = displayedDateString,
                                mealType = mealDropdown.selectedItem.toString()
                            ))
                        }
                        runOnUiThread { refreshDiary() }
                    }
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