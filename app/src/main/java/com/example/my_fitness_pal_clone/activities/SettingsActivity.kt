package com.example.my_fitness_pal_clone.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.toColorInt
import com.example.my_fitness_pal_clone.utils.DataManager

class SettingsActivity : AppCompatActivity() {

    private val colorPrimary = "#0066EE".toColorInt()
    private val colorBackground = "#F5F7FA".toColorInt()
    private val colorCard = Color.WHITE
    private val colorTextPrimary = "#111827".toColorInt()
    private val colorTextSecondary = "#6B7280".toColorInt()
    private val colorDivider = "#E5E7EB".toColorInt()
    private val colorInputBg = "#F9FAFB".toColorInt()

    private lateinit var goalInput: EditText
    private lateinit var proteinInput: EditText
    private lateinit var carbsInput: EditText
    private lateinit var fatsInput: EditText

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataManager.loadData(this)

        val root = RelativeLayout(this).apply {
            setBackgroundColor(colorBackground)
        }

        val scrollView = ScrollView(this).apply {
            isScrollbarFadingEnabled = false
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // Responsive padding
            setPadding(20.dp(), 30.dp(), 20.dp(), 30.dp())
        }

        contentLayout.addView(createTopBar())
        contentLayout.addView(createCalorieCard())
        contentLayout.addView(createMacrosCard())
        contentLayout.addView(createSaveButton())

        scrollView.addView(contentLayout)
        root.addView(scrollView, RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        setContentView(root)
    }

    @SuppressLint("SetTextI18n")
    private fun createTopBar(): View {
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 30.dp())
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        topBar.addView(TextView(this).apply {
            text = "← Back"
            textSize = 18f
            setTextColor(colorPrimary)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 5.dp(), 15.dp(), 5.dp())
            setOnClickListener { finish() }
        })

        topBar.addView(TextView(this).apply {
            text = "Settings"
            textSize = 28f
            setTextColor(colorTextPrimary)
            setTypeface(null, Typeface.BOLD)
        })

        return topBar
    }

    @SuppressLint("SetTextI18n")
    private fun createCalorieCard(): View {
        val card = createBaseCard()

        card.addView(TextView(this).apply {
            text = "Daily Calorie Goal"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            setPadding(0, 0, 0, 10.dp())
        })

        goalInput = createStyledInput("e.g. 2000").apply {
            setText(DataManager.dailyGoal.toString())
        }
        card.addView(goalInput)

        return card
    }

    @SuppressLint("SetTextI18n")
    private fun createMacrosCard(): View {
        val card = createBaseCard()

        card.addView(TextView(this).apply {
            text = "Macronutrient Distribution (%)"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            setPadding(0, 0, 0, 5.dp())
        })

        card.addView(TextView(this).apply {
            text = "Must equal exactly 100%"
            textSize = 14f
            setTextColor(colorTextSecondary)
            setPadding(0, 0, 0, 15.dp())
        })

        proteinInput = createMacroRow(card, "Protein (%)", DataManager.proteinPct)
        carbsInput = createMacroRow(card, "Carbs (%)", DataManager.carbsPct)
        fatsInput = createMacroRow(card, "Fats (%)", DataManager.fatsPct)

        return card
    }

    private fun createMacroRow(parent: LinearLayout, labelText: String, defaultValue: Int): EditText {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 10.dp())
            }
            gravity = Gravity.CENTER_VERTICAL
            weightSum = 2f
        }

        row.addView(TextView(this).apply {
            text = labelText
            textSize = 16f
            setTextColor(colorTextPrimary)
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        })

        val input = createStyledInput("0").apply {
            setText(defaultValue.toString())
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
        }

        row.addView(input)
        parent.addView(row)

        return input
    }

    @SuppressLint("SetTextI18n")
    private fun createSaveButton(): View {
        return AppCompatButton(this).apply {
            text = "Save Settings"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = 10.dp()
            }
            // Customizing height for a more premium look
            minHeight = 56.dp()
            background = GradientDrawable().apply {
                setColor(colorPrimary)
                cornerRadius = 15.dp().toFloat()
            }
            setOnClickListener { saveSettings() }
        }
    }

    private fun createBaseCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25.dp(), 25.dp(), 25.dp(), 25.dp())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }
            background = GradientDrawable().apply {
                setColor(colorCard)
                cornerRadius = 15.dp().toFloat()
            }
            elevation = 4.dp().toFloat()
        }
    }

    private fun createStyledInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_NUMBER
            background = GradientDrawable().apply {
                setColor(colorInputBg)
                cornerRadius = 8.dp().toFloat()
                setStroke(1.dp(), colorDivider)
            }
            setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
            setTextColor(colorTextPrimary)
            setHintTextColor(colorTextSecondary)
            textSize = 16f
        }
    }

    private fun saveSettings() {
        val newGoal = goalInput.text.toString().toIntOrNull()
        val p = proteinInput.text.toString().toIntOrNull() ?: 0
        val c = carbsInput.text.toString().toIntOrNull() ?: 0
        val f = fatsInput.text.toString().toIntOrNull() ?: 0

        if (newGoal == null || newGoal <= 0) {
            Toast.makeText(this, "Please enter a valid calorie goal", Toast.LENGTH_SHORT).show()
            return
        }

        if (p + c + f != 100) {
            Toast.makeText(this, "Macros must equal exactly 100% total", Toast.LENGTH_SHORT).show()
            return
        }

        DataManager.dailyGoal = newGoal
        DataManager.proteinPct = p
        DataManager.carbsPct = c
        DataManager.fatsPct = f
        DataManager.saveData(this)

        Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}