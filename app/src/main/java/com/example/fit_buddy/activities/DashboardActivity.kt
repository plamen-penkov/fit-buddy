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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.toColorInt
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.fit_buddy.utils.DataManager
import com.example.fit_buddy.utils.setupBottomNavigation

class DashboardActivity : AppCompatActivity() {

    private lateinit var dashboardContent: LinearLayout
    private lateinit var calorieInfo: TextView
    private lateinit var progressView: CircularProgressView
    private lateinit var baseGoalText: TextView
    private lateinit var consumedText: TextView

    private val colorPrimary = "#0066EE".toColorInt()
    private val colorBackground = "#F5F7FA".toColorInt()
    private val colorCard = Color.WHITE
    private val colorTextPrimary = "#111827".toColorInt()
    private val colorTextSecondary = "#6B7280".toColorInt()
    private val colorDivider = "#E5E7EB".toColorInt()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        DataManager.loadData(this)

        val root = ConstraintLayout(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setBackgroundColor(colorBackground)
        }

        val scrollView = ScrollView(this).apply {
            id = View.generateViewId()
            isScrollbarFadingEnabled = false
            layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, 0) // Height 0 means "match constraints"
        }

        dashboardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 30.dp(), 20.dp(), 30.dp())
        }
        scrollView.addView(dashboardContent)
        root.addView(scrollView)

        val set = ConstraintSet()
        set.clone(root)

        set.connect(scrollView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(scrollView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(scrollView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        set.connect(scrollView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.setMargin(scrollView.id, ConstraintSet.BOTTOM, 60.dp())

        set.applyTo(root)

        buildDashboardUi()

        setupBottomNavigation(this, root, 1)
        setContentView(root)
    }

    private fun buildDashboardUi() {
        dashboardContent.removeAllViews()
        dashboardContent.addView(createHeaderTitle())
        dashboardContent.addView(createProgressCard())
    }

    @SuppressLint("SetTextI18n")
    private fun createHeaderTitle(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }

            addView(TextView(this@DashboardActivity).apply {
                text = "Dashboard"
                textSize = 28f
                setTextColor(colorTextPrimary)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@DashboardActivity).apply {
                text = "Welcome back!"
                textSize = 16f
                setTextColor(colorTextSecondary)
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createProgressCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 24.dp(), 20.dp(), 24.dp())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 20.dp())
            }
            background = GradientDrawable().apply {
                setColor(colorCard)
                cornerRadius = 16.dp().toFloat()
            }
            elevation = 4.dp().toFloat()
        }

        card.addView(TextView(this).apply {
            text = "Daily Calorie Goal"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            setPadding(0, 0, 0, 16.dp())
        })

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            weightSum = 3f
            gravity = Gravity.CENTER_VERTICAL
        }

        val circleSize = 200.dp()
        val circleContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(circleSize, circleSize).apply {
                setMargins(0, 0, 12.dp(), 0)
            }
        }

        progressView = CircularProgressView(this).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        val innerCircleTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            gravity = Gravity.CENTER
        }

        calorieInfo = TextView(this).apply {
            text = "0"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorPrimary)
        }

        innerCircleTextLayout.addView(calorieInfo)
        innerCircleTextLayout.addView(TextView(this).apply {
            text = "Remaining"
            textSize = 12f
            setTextColor(colorTextSecondary)
        })

        circleContainer.addView(progressView)
        circleContainer.addView(innerCircleTextLayout)

        val leftWrapper = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 2f)
            gravity = Gravity.CENTER
            addView(circleContainer)
        }

        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)

            addView(TextView(this@DashboardActivity).apply {
                text = "Base Goal"; textSize = 12f; setTextColor(colorTextSecondary)
            })
            baseGoalText = TextView(this@DashboardActivity).apply {
                text = "0 kcal"; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(colorTextPrimary)
                setPadding(0, 2.dp(), 0, 12.dp())
            }
            addView(baseGoalText)

            addView(TextView(this@DashboardActivity).apply {
                text = "Consumed"; textSize = 12f; setTextColor(colorTextSecondary)
            })
            consumedText = TextView(this@DashboardActivity).apply {
                text = "0 kcal"; textSize = 16f; setTypeface(null, Typeface.BOLD); setTextColor(colorPrimary)
            }
            addView(consumedText)
        }

        mainLayout.addView(leftWrapper)
        mainLayout.addView(statsContainer)
        card.addView(mainLayout)

        return card
    }

    inner class CircularProgressView(context: Context) : View(context) {
        var progress: Float = 0f
            set(value) { field = value.coerceIn(0f, 1f); invalidate() }
        private val strokeW = 10.dp().toFloat()
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorDivider; style = Paint.Style.STROKE; strokeWidth = strokeW
        }
        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary; style = Paint.Style.STROKE; strokeWidth = strokeW; strokeCap = Paint.Cap.ROUND
        }
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val rect = RectF(strokeW/2, strokeW/2, width - strokeW/2, height - strokeW/2)
            canvas.drawArc(rect, 0f, 360f, false, bgPaint)
            canvas.drawArc(rect, -90f, 360f * progress, false, progressPaint)
        }
    }

    override fun onResume() {
        super.onResume()
        DataManager.loadData(this)
        updateProgressUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressUI() {
        if (::calorieInfo.isInitialized) {
            val remaining = DataManager.getRemainingCalories()
            val consumed = DataManager.getTotalConsumedForToday()
            val dailyGoal = DataManager.dailyGoal
            calorieInfo.text = "$remaining"
            baseGoalText.text = "$dailyGoal kcal"
            consumedText.text = "$consumed kcal"
            progressView.progress = if (dailyGoal > 0) consumed.toFloat() / dailyGoal.toFloat() else 0f
        }
    }
}