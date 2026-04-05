package com.example.my_fitness_pal_clone.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FoodItem(
    val name: String,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val date: String,
    val mealType: String
) {
    // Automatically calculate the calories based on the macros
    val calories: Int
        get() = (protein * 4) + (carbs * 4) + (fats * 9)
}

object DataManager {
    private const val TAG = "DataManager"
    private const val PREFS_NAME =  "fitness_prefs"
    private const val FOODS_KEY =  "saved_foods"
    private const val GOAL_KEY =  "daily_goal"

    private const val PROTEIN_PCT_KEY = "protein_pct"
    private const val CARBS_PCT_KEY = "carbs_pct"
    private const val FATS_PCT_KEY = "fats_pct"

    var dailyGoal: Int = 2000
    var proteinPct: Int = 30
    var carbsPct = 40
    var fatsPct = 30
    var consumedFoods = mutableListOf<FoodItem>()
        private set

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

        val foodsJson = prefs.getString(FOODS_KEY, null)
        if (!foodsJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<MutableList<FoodItem>>() {}.type
                val parsedFoods: MutableList<FoodItem>? = Gson().fromJson(foodsJson, type)
                if (parsedFoods != null) {
                    consumedFoods = parsedFoods
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing food data from SharedPreferences", e)
                consumedFoods = mutableListOf()
            }
        }
    }

    fun saveData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt(GOAL_KEY, dailyGoal)
        editor.putInt(PROTEIN_PCT_KEY, proteinPct)
        editor.putInt(CARBS_PCT_KEY, carbsPct)
        editor.putInt(FATS_PCT_KEY, fatsPct)

        try {
            val foodsJson = Gson().toJson(consumedFoods)
            editor.putString(FOODS_KEY, foodsJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error serializing food data", e)
        }

        editor.apply()
    }

    fun addFood(context: Context, foodItem: FoodItem) {
        consumedFoods.add(foodItem)
        saveData(context)
    }

    fun removeFood(context: Context, foodItem: FoodItem) {
        consumedFoods.remove(foodItem)
        saveData(context)
    }

    fun getTodayFoods(): List<FoodItem> {
        val today = getCurrentDate()
        return consumedFoods.filter { it.date == today }
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