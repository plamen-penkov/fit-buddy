package com.example.fit_buddy.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.fit_buddy.R
import com.example.fit_buddy.utils.DataManager
import com.example.fit_buddy.utils.setupBottomNavigation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class StatsActivity : AppCompatActivity() {

    private val colorBackground by lazy { getColor(R.color.app_background) }
    private val colorCard by lazy { getColor(R.color.app_surface) }
    private val colorTextPrimary by lazy { getColor(R.color.app_on_surface) }
    private val colorTextSecondary by lazy { getColor(R.color.app_on_surface_variant) }
    private val colorDivider by lazy { getColor(R.color.app_outline) }

    private lateinit var chartView: BarChartView
    private lateinit var chartTitle: TextView
    private lateinit var toggleContainer: LinearLayout

    private val executor = Executors.newSingleThreadExecutor()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    enum class StatType(val title: String, val color: Int) {
        CALORIES("Calories", "#0066EE".toColorInt()),
        PROTEIN("Protein", "#d97b23".toColorInt()),
        CARBS("Carbs", "#10692a".toColorInt()),
        FATS("Fats", "#611596".toColorInt())
    }

    private var currentStatType = StatType.CALORIES

    data class ChartPoint(val label: String, val value: Float)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        contentLayout.addView(createToggleRow())
        contentLayout.addView(createChartCard())

        scrollView.addView(contentLayout)

        val scrollParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            bottomMargin = 80.dp() // Responsive space for bottom nav
        }
        root.addView(scrollView, scrollParams)

        setupBottomNavigation(this, root, 3)
        setContentView(root)

        executor.execute {
            DataManager.loadData(this)
            runOnUiThread { updateChartData() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createHeaderTitle(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }

            addView(TextView(this@StatsActivity).apply {
                text = "Weekly Stats"
                textSize = 28f
                setTextColor(colorTextPrimary)
                setTypeface(null, Typeface.BOLD)
            })
        }
    }

    private fun createToggleRow(): View {
        toggleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 25.dp())
            }
            background = GradientDrawable().apply {
                setColor(colorDivider)
                cornerRadius = 25.dp().toFloat()
            }
            setPadding(5.dp(), 5.dp(), 5.dp(), 5.dp())
            weightSum = 4f
        }

        StatType.entries.forEach { type ->
            val btn = TextView(this).apply {
                text = type.name.lowercase().replaceFirstChar { it.uppercase() }
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(0, 12.dp(), 0, 12.dp())
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)

                setOnClickListener {
                    currentStatType = type
                    updateToggleUI()
                    updateChartData()
                }
            }
            toggleContainer.addView(btn)
        }

        updateToggleUI()
        return toggleContainer
    }

    private fun updateToggleUI() {
        for (i in 0 until toggleContainer.childCount) {
            val child = toggleContainer.getChildAt(i) as TextView
            val type = StatType.entries[i]

            if (type == currentStatType) {
                child.setTextColor(Color.WHITE)
                child.setTypeface(null, Typeface.BOLD)
                child.background = GradientDrawable().apply {
                    setColor(type.color)
                    cornerRadius = 20.dp().toFloat()
                }
            } else {
                child.setTextColor(colorTextSecondary)
                child.setTypeface(null, Typeface.NORMAL)
                child.background = null
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createChartCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 25.dp(), 20.dp(), 25.dp())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            background = GradientDrawable().apply {
                setColor(colorCard)
                cornerRadius = 20.dp().toFloat()
            }
            elevation = 4.dp().toFloat()
        }

        chartTitle = TextView(this).apply {
            text = "Calories Consumed"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            setPadding(5.dp(), 0, 0, 20.dp())
        }
        card.addView(chartTitle)

        chartView = BarChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 250.dp())
        }
        card.addView(chartView)

        return card
    }

    @SuppressLint("SetTextI18n")
    private fun updateChartData() {
        chartTitle.text = "${currentStatType.title} (Last 7 Days)"
        val points = mutableListOf<ChartPoint>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        (0..6).forEach { _ ->
            val dateStr = dateFormat.format(calendar.time)
            val foodsForDay = DataManager.consumedFoods.filter { it.date == dateStr }
            val value = when (currentStatType) {
                StatType.CALORIES -> foodsForDay.sumOf { it.calories }.toFloat()
                StatType.PROTEIN -> foodsForDay.sumOf { it.protein }.toFloat()
                StatType.CARBS -> foodsForDay.sumOf { it.carbs }.toFloat()
                StatType.FATS -> foodsForDay.sumOf { it.fats }.toFloat()
            }
            points.add(ChartPoint(labelFormat.format(calendar.time), value))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        chartView.setData(points, currentStatType.color)
    }

    inner class BarChartView(context: Context) : View(context) {
        private var dataPoints: List<ChartPoint> = emptyList()
        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // Responsive Paint Sizes
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextSecondary
            textSize = 12.dp().toFloat()
            textAlign = Paint.Align.CENTER
        }
        private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextPrimary
            textSize = 11.dp().toFloat()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        private val rectF = RectF()

        fun setData(points: List<ChartPoint>, barColor: Int) {
            this.dataPoints = points
            this.barPaint.color = barColor
            invalidate()
        }

        @SuppressLint("DefaultLocale")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (dataPoints.isEmpty()) return

            val maxVal = dataPoints.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1f
            val paddingBottom = 30.dp().toFloat()
            val paddingTop = 30.dp().toFloat()
            val usableHeight = height - paddingBottom - paddingTop
            val slotWidth = width.toFloat() / dataPoints.size
            val barWidth = slotWidth * 0.6f
            val spacing = (slotWidth - barWidth) / 2f

            for (i in dataPoints.indices) {
                val point = dataPoints[i]
                val left = (i * slotWidth) + spacing
                val right = left + barWidth
                val barHeight = (point.value / maxVal) * usableHeight
                val bottom = height - paddingBottom
                val top = bottom - barHeight

                rectF.set(left, top, right, bottom)
                canvas.drawRoundRect(rectF, 6.dp().toFloat(), 6.dp().toFloat(), barPaint)

                val textX = left + (barWidth / 2f)
                canvas.drawText(point.label, textX, height - 5.dp().toFloat(), textPaint)

                val displayVal = if (point.value % 1 == 0f) point.value.toInt().toString() else String.format("%.1f", point.value)
                canvas.drawText(displayVal, textX, top - 8.dp().toFloat(), valuePaint)
            }
        }
    }
}