package com.example.my_fitness_pal_clone.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.toColorInt
import com.example.my_fitness_pal_clone.activities.DashboardActivity
import com.example.my_fitness_pal_clone.activities.DiaryActivity
import com.example.my_fitness_pal_clone.activities.MoreActivity
import com.example.my_fitness_pal_clone.activities.StatsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun setupBottomNavigation(context: Context, rootLayout: ViewGroup, currentId: Int) {
    val bottomNav = BottomNavigationView(context).apply {
        id = View.generateViewId()
        setBackgroundColor(Color.WHITE)
        elevation = 20f

        val colorPrimary = "#0066EE".toColorInt()
        val colorUnselected = "#6B7280".toColorInt()

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(colorPrimary, colorUnselected)
        val colorStateList = ColorStateList(states, colors)

        itemIconTintList = colorStateList
        itemTextColor = colorStateList

        // Setup Menu Items
        menu.add(0, 1, 0, "Dashboard").setIcon(android.R.drawable.ic_menu_today)
        menu.add(0, 2, 1, "Diary").setIcon(android.R.drawable.ic_menu_agenda)
        menu.add(0, 3, 2, "Stats").setIcon(android.R.drawable.ic_menu_compass)
        menu.add(0, 4, 3, "More").setIcon(android.R.drawable.ic_menu_manage)

        selectedItemId = currentId

        setOnItemSelectedListener { item ->
            @Suppress("DEPRECATION")
            fun navigateWithoutAnimation(targetClass: Class<*>) {
                val intent = Intent(context, targetClass)

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)

                if (context is Activity) {
                    context.overridePendingTransition(0, 0)
                    context.finish()
                }
            }

            when (item.itemId) {
                1 -> if (currentId != 1) navigateWithoutAnimation(DashboardActivity::class.java)
                2 -> if (currentId != 2) navigateWithoutAnimation(DiaryActivity::class.java)
                3 -> if (currentId != 3) navigateWithoutAnimation(StatsActivity::class.java)
                4 -> if (currentId != 4) navigateWithoutAnimation(MoreActivity::class.java)
            }
            true
        }
    }

    val params = when (rootLayout) {
        is ConstraintLayout -> {
            ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomToBottom = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            }
        }
        is RelativeLayout -> {
            RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }
        else -> {
            ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    rootLayout.addView(bottomNav, params)
}