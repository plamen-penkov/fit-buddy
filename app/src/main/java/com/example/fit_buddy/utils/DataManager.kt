package com.example.fit_buddy.utils

import android.content.Context
import android.util.Log
import com.example.fit_buddy.data.AppDatabase
import com.example.fit_buddy.data.FoodItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataManager {
    private const val TAG = "DataManager"
    private const val PREFS_NAME =  "fitness_prefs"
    private const val GOAL_KEY =  "daily_goal"

    private const val PROTEIN_PCT_KEY = "protein_pct"
    private const val CARBS_PCT_KEY = "carbs_pct"
    private const val FATS_PCT_KEY = "fats_pct"
    private const val THEME_KEY = "theme_mode"

    var dailyGoal: Int = 2000
    var proteinPct: Int = 30
    var carbsPct: Int = 40
    var fatsPct: Int = 30
    var themeMode: Int = -1  // -1 = system default, 1 = light, 2 = dark

    // In-memory cache loaded from Room
    var consumedFoods = mutableListOf<FoodItemEntity>()
        private set

    private fun getDao(context: Context) = AppDatabase.getInstance(context).foodDao()

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun loadData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        dailyGoal = prefs.getInt(GOAL_KEY,  2000)
        proteinPct = prefs.getInt(PROTEIN_PCT_KEY, 30)
        carbsPct = prefs.getInt(CARBS_PCT_KEY, 40)
        fatsPct = prefs.getInt(FATS_PCT_KEY, 30)
        themeMode = prefs.getInt(THEME_KEY, -1)

        // Load all foods from Room database
        try {
            val dao = getDao(context)
            consumedFoods = dao.getAll().toMutableList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading food data from Room", e)
            consumedFoods = mutableListOf()
        }
    }

    fun saveSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(GOAL_KEY, dailyGoal)
        editor.putInt(PROTEIN_PCT_KEY, proteinPct)
        editor.putInt(CARBS_PCT_KEY, carbsPct)
        editor.putInt(FATS_PCT_KEY, fatsPct)
        editor.putInt(THEME_KEY, themeMode)
        editor.apply()
    }

    fun addFood(context: Context, food: FoodItemEntity): Long {
        return try {
            val dao = getDao(context)
            val id = dao.insert(food)
            val insertedFood = food.copy(id = id)
            consumedFoods.add(0, insertedFood)
            id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting food into Room", e)
            -1
        }
    }

    fun updateFood(context: Context, food: FoodItemEntity) {
        try {
            val dao = getDao(context)
            dao.update(food)
            val index = consumedFoods.indexOfFirst { it.id == food.id }
            if (index >= 0) {
                consumedFoods[index] = food
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating food in Room", e)
        }
    }

    fun removeFood(context: Context, food: FoodItemEntity) {
        try {
            val dao = getDao(context)
            dao.delete(food)
            consumedFoods.removeAll { it.id == food.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting food from Room", e)
        }
    }

    fun getFoodsForDate(date: String): List<FoodItemEntity> {
        return consumedFoods.filter { it.date == date }
    }

    fun getTodayFoods(): List<FoodItemEntity> {
        val today = getCurrentDate()
        return getFoodsForDate(today)
    }

    fun getTotalConsumedForToday(): Int {
        return getTodayFoods().sumOf { it.calories }
    }

    fun getRemainingCalories(): Int {
        val remaining = dailyGoal - getTotalConsumedForToday()
        return if (remaining < 0) 0 else remaining
    }

    fun getTargetMacrosGrams(): Triple<Int, Int, Int> {
        val targetProteinCals = dailyGoal * (proteinPct / 100f)
        val targetCarbsCals = dailyGoal * (carbsPct / 100f)
        val targetFatsCals = dailyGoal * (fatsPct / 100f)

        val proteinGrams = (targetProteinCals / 4).toInt()
        val carbsGrams = (targetCarbsCals / 4).toInt()
        val fatsGrams = (targetFatsCals / 9).toInt()

        return Triple(proteinGrams, carbsGrams, fatsGrams)
    }

    fun getConsumedMacrosForToday(): Triple<Int, Int, Int> {
        val todayFoods = getTodayFoods()
        val consumedP = todayFoods.sumOf { it.protein }
        val consumedC = todayFoods.sumOf { it.carbs }
        val consumedF = todayFoods.sumOf { it.fats }

        return Triple(consumedP, consumedC, consumedF)
    }
}