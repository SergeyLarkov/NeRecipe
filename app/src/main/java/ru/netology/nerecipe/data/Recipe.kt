package ru.netology.nerecipe.data

data class Recipe(
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val categoryId: Int,
    val category: String,
    val name: String,
    val ingredients: String,
    val cookingProcess: String,
    val myGrade: Int,
    val totalGrade: Int,
    val commentsCount: Long,
    val isFavorite: Boolean,
    val imageId: Long
)
