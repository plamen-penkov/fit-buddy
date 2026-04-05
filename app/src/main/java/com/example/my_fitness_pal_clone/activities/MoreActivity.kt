package com.example.my_fitness_pal_clone.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.my_fitness_pal_clone.utils.setupBottomNavigation

class MoreActivity : AppCompatActivity() {

    private val colorPrimary = "#0066EE".toColorInt()
    private val colorBackground = "#F5F7FA".toColorInt()
    private val colorCard = Color.WHITE
    private val colorTextPrimary = "#111827".toColorInt()
    private val colorTextSecondary = "#6B7280".toColorInt()
    private val colorDivider = "#E5E7EB".toColorInt()

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    @SuppressLint("SetTextI18n")
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
}