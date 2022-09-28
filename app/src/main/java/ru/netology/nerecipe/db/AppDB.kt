package ru.netology.nerecipe.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ru.netology.nerecipe.db.RecipeDB.DBStructure.CREATE_COMMAND

class AppDb private constructor(db: SQLiteDatabase) {
    val recipeDb = RecipeDB(db)

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: AppDb(
                    buildDb(context, CREATE_COMMAND)
                ).also { instance = it }
            }
        }

        private fun buildDb(context: Context, DDLs: Array<String>) = DbHelper(
            context, 1, "Recipe.db", DDLs
        ).writableDatabase
    }

}