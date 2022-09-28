package ru.netology.nerecipe.db

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nerecipe.data.FilterValues
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.RecipeRepository

class SqlRecipeRepository(private val dao: RecipeDB): RecipeRepository {
    private var recipes: List<Recipe> = emptyList()
    private val data = MutableLiveData(recipes)

    init {
        recipes = listOf()
        data.value = recipes
    }

    override fun setValue() {
        recipes  = dao.getAll()
        data.value = recipes
    }

    override fun getAll(): LiveData<List<Recipe>> = data

    override fun getAllRecipes() {
        data.value = recipes
    }

    override fun getFavoriteRecipes() {
        data.value = recipes.filter { it.isFavorite }
    }

    override fun getRecipe(id: Long): Recipe? {
        return recipes.find { recipe: Recipe -> recipe.id == id }
    }

    override fun findRecipeByName(searchText: String, inFavoriteOnly: Boolean) {
        data.value = recipes.filter {
            (it.name.indexOf(searchText,0,true) >= 0) &&
            ((!inFavoriteOnly) || (it.isFavorite))
        }
    }

    override fun findRecipeByFilter(filterValues: FilterValues) {
        data.value = recipes.filter {
            (filterValues.categories.indexOf(it.categoryId) >= 0) &&
            (it.totalGrade >= filterValues.grade) &&
            ((filterValues.userid == 0L) || (it.authorId == filterValues.userid)) &&
            ((!filterValues.withComments) || (it.commentsCount > 0))
        }
    }

    override fun new(recipe: Recipe, bitmap: Bitmap?) {
        val newRecipe = dao.new(recipe, bitmap)
        recipes = listOf(newRecipe) + recipes
        data.value = recipes
    }

    override fun delete(recipeId: Long) {
        dao.delete(recipeId)
        recipes = recipes.filter { it.id !=recipeId }
        data.value = recipes
    }

    override fun edit(recipe: Recipe, bitmap: Bitmap?) {
        dao.edit(recipe, bitmap)
        recipes = recipes.map { if (it.id == recipe.id) recipe else it }
        data.value = recipes
    }

    override fun getComments(recipeId: Long): ArrayList<String> = dao.getComments(recipeId)

    override fun addComment(recipeId: Long, text: String) {
        dao.addComment(recipeId, text)
        recipes = recipes.map { if (it.id == recipeId) it.copy(commentsCount = it.commentsCount + 1) else it }
        data.value = recipes
    } 

    override fun deleteComment(commentId: Long) = dao.deleteComment(commentId)

    override fun deleteComment(commentText: String) = dao.deleteComment(commentText)

    override fun getCategories() = dao.getCategories()

    override fun setFavorite(recipeId: Long, isFavorite: Boolean) {
        dao.setFavorite(recipeId, isFavorite)
        recipes = recipes.map { if (it.id == recipeId) it.copy(isFavorite = isFavorite) else it }
        data.value = recipes
    }

    override fun updateGrade(recipeId: Long, grade: Int) {
        dao.updateGrade(recipeId, grade)
        recipes = recipes.map { if (it.id == recipeId) it.copy(myGrade = grade, totalGrade = dao.getTotalGrade(recipeId)) else it }
        data.value = recipes
    }

    override fun getUserId(userName:String): Long = dao.getUserId(userName)

    override fun getUsers() = dao.getUsers()

    override fun getImage(imageId: Long): Bitmap? = dao.getImage(imageId)
}