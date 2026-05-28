package com.example.fit_buddy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(food: FoodItemEntity): Long

    @Update
    fun update(food: FoodItemEntity)

    @Delete
    fun delete(food: FoodItemEntity)

    @Query("SELECT * FROM food_items ORDER BY id DESC")
    fun getAll(): List<FoodItemEntity>

    @Query("SELECT * FROM food_items WHERE date = :date ORDER BY id ASC")
    fun getByDate(date: String): List<FoodItemEntity>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    fun getById(id: Long): FoodItemEntity?

    @Query("DELETE FROM food_items WHERE id = :id")
    fun deleteById(id: Long)
}
