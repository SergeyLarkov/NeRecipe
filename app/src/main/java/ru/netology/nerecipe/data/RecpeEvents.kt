package ru.netology.nerecipe.data

import android.graphics.Bitmap

interface RecipeEvents {
    fun onFavoriteClicked(recipeId:Long, isFavorite:Boolean)
    fun onEditClicked(recipe: Recipe)
    fun onDeleteClicked(recipe: Recipe)
    fun onShowClicked(recipe: Recipe)
    fun getImage(imageId:Long): Bitmap?
}