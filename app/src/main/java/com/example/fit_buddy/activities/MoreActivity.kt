package com.example.fit_buddy.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.fit_buddy.R
import com.example.fit_buddy.utils.DataManager
import com.example.fit_buddy.utils.setupBottomNavigation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class MoreActivity : AppCompatActivity() {

    private val colorPrimary by lazy { getColor(R.color.app_primary) }
    private val colorBackground by lazy { getColor(R.color.app_background) }
    private val colorCard by lazy { getColor(R.color.app_surface) }
    private val colorTextPrimary by lazy { getColor(R.color.app_on_surface) }
    private val colorTextSecondary by lazy { getColor(R.color.app_on_surface_variant) }
    private val colorDivider by lazy { getColor(R.color.app_outline) }

    private val executor = Executors.newSingleThreadExecutor()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor.execute {
            DataManager.loadData(this)
            runOnUiThread { buildUI() }
        }
    }

    private fun buildUI() {
        val root = RelativeLayout(this).apply {
            setBackgroundColor(colorBackground)
        }

        val scrollView = ScrollView(this).apply {
            id = View.generateViewId()
            isScrollbarFadingEnabled = false
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 30.dp(), 20.dp(), 30.dp())
        }

        contentLayout.addView(createHeaderTitle())

        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(colorCard)
                cornerRadius = 15.dp().toFloat()
            }
            elevation = 4.dp().toFloat()
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 10.dp(), 0, 20.dp())
            }
            setPadding(5.dp(), 10.dp(), 5.dp(), 10.dp())
        }

        menuContainer.addView(createMenuOption(android.R.drawable.ic_menu_myplaces, "My Profile") {
            // TODO: Navigate to Profile Activity
        })

        menuContainer.addView(createDivider())
        menuContainer.addView(createMenuOption(android.R.drawable.ic_menu_preferences, "Settings (Goals & Macros)") {
            startActivity(Intent(this@MoreActivity, SettingsActivity::class.java))
        })

        menuContainer.addView(createDivider())
        menuContainer.addView(createMenuOption(android.R.drawable.ic_menu_send, "Export & Share Report") {
            showShareDatePicker()
        })

        menuContainer.addView(createDivider())
        menuContainer.addView(createMenuOption(android.R.drawable.ic_menu_view, "Theme") {
            showThemeDialog()
        })

        menuContainer.addView(createDivider())
        menuContainer.addView(createMenuOption(android.R.drawable.ic_popup_reminder, "Reminders") {
            // TODO: Navigate to Reminders
        })

        contentLayout.addView(menuContainer)

        val scrollParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            bottomMargin = 80.dp()
        }

        scrollView.addView(contentLayout)
        root.addView(scrollView, scrollParams)

        setupBottomNavigation(this, root, 4)
        setContentView(root)
    }

    @SuppressLint("SetTextI18n")
    private fun createHeaderTitle(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }

            addView(TextView(this@MoreActivity).apply {
                text = "More"
                textSize = 32f
                setTextColor(colorTextPrimary)
                setTypeface(null, Typeface.BOLD)
            })
        }
    }

    private fun createMenuOption(iconRes: Int, title: String, onClick: () -> Unit): View {
        val optionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(20.dp(), 22.dp(), 20.dp(), 22.dp())
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            setOnClickListener { onClick() }
        }

        optionLayout.addView(ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@MoreActivity, iconRes))
            setColorFilter(colorPrimary)
            layoutParams = LinearLayout.LayoutParams(24.dp(), 24.dp()).apply {
                setMargins(0, 0, 20.dp(), 0)
            }
        })

        optionLayout.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(colorTextPrimary)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        })

        optionLayout.addView(TextView(this).apply {
            text = "›"
            textSize = 24f
            setTextColor(colorTextSecondary)
            setPadding(10.dp(), 0, 0, 0)
            gravity = Gravity.CENTER
        })

        return optionLayout
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 1.dp()).apply {
                setMargins(20.dp(), 0, 20.dp(), 0)
            }
            setBackgroundColor(colorDivider)
        }
    }

    // ─── Theme Toggle ────────────────────────────────────────────────────

    private fun showThemeDialog() {
        val options = arrayOf("System Default", "Light", "Dark")
        val currentSelection = when (DataManager.themeMode) {
            1 -> 1
            2 -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                val mode = when (which) {
                    1 -> 1   // Light
                    2 -> 2   // Dark
                    else -> -1 // System
                }
                DataManager.themeMode = mode
                executor.execute {
                    DataManager.saveSettings(this)
                }
                when (mode) {
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Share Intent ────────────────────────────────────────────────────

    private fun showShareDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                shareReportForDate(dateStr, cal)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    @SuppressLint("SetTextI18n")
    private fun shareReportForDate(dateStr: String, cal: Calendar) {
        val foods = DataManager.consumedFoods.filter { it.date == dateStr }
        val displayDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(cal.time)

        val totalCals = foods.sumOf { it.calories }
        val totalP = foods.sumOf { it.protein }
        val totalC = foods.sumOf { it.carbs }
        val totalF = foods.sumOf { it.fats }

        val sb = StringBuilder()
        sb.appendLine("📊 FitBuddy Report — $displayDate")
        sb.appendLine("🔥 Calories: $totalCals / ${DataManager.dailyGoal} kcal")
        sb.appendLine("💪 Protein: ${totalP}g | 🌾 Carbs: ${totalC}g | 🧈 Fats: ${totalF}g")
        sb.appendLine()

        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
        val mealEmojis = mapOf("Breakfast" to "🍳", "Lunch" to "🥗", "Dinner" to "🍝", "Snacks" to "🍿")

        mealTypes.forEach { meal ->
            val mealFoods = foods.filter { it.mealType == meal }
            val mealCals = mealFoods.sumOf { it.calories }
            sb.appendLine("${mealEmojis[meal]} $meal: $mealCals kcal")
            mealFoods.forEach { food ->
                sb.appendLine("   • ${food.name} — ${food.calories} kcal")
            }
        }

        sb.appendLine()
        sb.appendLine("Tracked with FitBuddy 💪")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "FitBuddy Report — $displayDate")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(shareIntent, "Share Report via"))
    }
}