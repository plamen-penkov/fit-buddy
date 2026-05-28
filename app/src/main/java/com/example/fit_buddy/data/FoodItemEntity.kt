package com.example.fit_buddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val proteinPer100g: Int,
    val carbsPer100g: Int,
    val fatsPer100g: Int,
    val servings: Double,   // number of 100g portions (e.g. 2.0 = 200g)
    val date: String,       // "yyyy-MM-dd"
    val mealType: String    // "Breakfast" | "Lunch" | "Dinner" | "Snacks"
) {
    /** Total protein in grams after applying servings multiplier */
    val protein: Int
        get() = (proteinPer100g * servings).toInt()

    /** Total carbs in grams after applying servings multiplier */
    val carbs: Int
        get() = (carbsPer100g * servings).toInt()

    /** Total fats in grams after applying servings multiplier */
    val fats: Int
        get() = (fatsPer100g * servings).toInt()

    /** Total calories: (P×4) + (C×4) + (F×9) */
    val calories: Int
        get() = (protein * 4) + (carbs * 4) + (fats * 9)
}
