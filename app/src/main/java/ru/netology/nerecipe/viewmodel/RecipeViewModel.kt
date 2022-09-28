package ru.netology.nerecipe.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import ru.netology.nerecipe.data.FilterValues
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.RecipeEvents
import ru.netology.nerecipe.db.SqlRecipeRepository
import ru.netology.nerecipe.data.RecipeRepository
import ru.netology.nerecipe.data.SingleLiveEvent
import ru.netology.nerecipe.db.AppDb

class RecipeViewModel(application: Application): AndroidViewModel(application), RecipeEvents {
    private val repository: RecipeRepository = SqlRecipeRepository(
        dao = AppDb.getInstance(application).recipeDb
    )

    val data = repository.getAll()
    val filterValues = FilterValues()

    val editEvent = SingleLiveEvent<Recipe>()
    val deleteEvent = SingleLiveEvent<Recipe>()
    val showEvent = SingleLiveEvent<Recipe>()
    val applyFilterEvent = SingleLiveEvent<Unit>()

    override fun onFavoriteClicked(recipeId:Long, isFavorite:Boolean) {
        repository.setFavorite(recipeId, isFavorite)
        if (filterValues.favoriteSelected) getFavoriteRecipes()
    }

    fun updateGrade(recipeId: Long, grade: Int) {
        repository.updateGrade(recipeId, grade)
        if (filterValues.favoriteSelected) getFavoriteRecipes()
    }

    override fun onEditClicked(recipe: Recipe) {
        editEvent.value = recipe
    }

    override fun onDeleteClicked(recipe: Recipe) {
        deleteEvent.value = recipe
    }

    override fun onShowClicked(recipe: Recipe) {
        showEvent.value = recipe
    }

    override fun getImage(imageId:Long)= repository.getImage(imageId)

    fun getRecipe(id: Long) = repository.getRecipe(id)

    fun getCategories() = repository.getCategories()

    fun getUsers() = repository.getUsers()

    fun getComments(recipeId: Long): ArrayList<String> = repository.getComments(recipeId)

    fun addComment(recipeId: Long, text: String) = repository.addComment(recipeId, text)

    fun deleteComment(commentText: String) = repository.deleteComment(commentText)

    fun updateRecipe(recipe: Recipe, bitmap: Bitmap?) {
        if (recipe.id == 0L) {
            repository.new(recipe, bitmap)
        } else {
            repository.edit(recipe, bitmap)
        }
        if (filterValues.favoriteSelected) getFavoriteRecipes()
    }

    fun deleteRecipe(recipeId: Long) {
        repository.delete(recipeId)
        if (filterValues.favoriteSelected) getFavoriteRecipes()
    }

    fun findRecipeByName(searchText: String) =
        repository.findRecipeByName(searchText, filterValues.favoriteSelected)

    fun findRecipeByFilter() {
        repository.findRecipeByFilter(filterValues)
    }

    fun getAllRecipes() = repository.getAllRecipes()

    fun getFavoriteRecipes() = repository.getFavoriteRecipes()

    fun getUserid(userName: String): Long = repository.getUserId(userName)

    fun setValue() = repository.setValue()

}