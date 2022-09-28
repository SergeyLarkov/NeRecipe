package ru.netology.nerecipe.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData

interface RecipeRepository {
    fun setValue()
    fun getAll(): LiveData<List<Recipe>>
    fun getAllRecipes()
    fun getFavoriteRecipes()
    fun getRecipe(id: Long): Recipe?
    fun findRecipeByName(searchText: String, inFavoriteOnly: Boolean)
    fun findRecipeByFilter(filterValues: FilterValues)
    fun new(recipe:Recipe, bitmap:Bitmap?)
    fun delete(recipeId: Long)
    fun edit(recipe:Recipe, bitmap:Bitmap?)
    fun getComments(recipeId: Long): ArrayList<String>
    fun addComment(recipeId: Long, text:String)
    fun deleteComment(commentId: Long)
    fun deleteComment(commentText: String)
    fun getImage(imageId: Long): Bitmap?
    fun getCategories(): ArrayList<String>
    fun setFavorite(recipeId: Long, isFavorite: Boolean)
    fun updateGrade(recipeId: Long, grade: Int)
    fun getUserId(userName:String): Long
    fun getUsers(): ArrayList<String>
}