package ru.netology.nerecipe.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.currentUser
import ru.netology.nerecipe.utils.bitmap2String
import kotlin.collections.ArrayList

class RecipeDB(private var db: SQLiteDatabase) {
    companion object DBStructure {
        const val USER_TABLE = "user"
        const val USER_ID = "id"
        const val USER_NAME = "name"

        const val CATEGORY_TABLE = "category"
        const val CATEGORY_ID = "id"
        const val CATEGORY_NAME = "name"

        const val RECIPE_TABLE = "recipe"
        const val RECIPE_ID = "id"
        const val RECIPE_AUTHOR_ID = "authorid"
        const val RECIPE_CATEGORY_ID = "categoryid"
        const val RECIPE_NAME = "name"
        const val RECIPE_INGREDIENTS = "ingredients"
        const val RECIPE_COOKING = "cooking"

        const val IMAGE_TABLE = "image"
        const val IMAGE_ID = "id"
        const val IMAGE_RECIPE_ID = "recipeid"
        const val IMAGE_NUM = "imagenum"
        const val IMAGE_IMAGE = "image"

        const val COMMENT_TABLE = "comment"
        const val COMMENT_ID = "id"
        const val COMMENT_RECIPE_ID = "recipeid"
        const val COMMENT_RECIPE_GRADE = "grade"
        const val COMMENT_TEXT = "text"
        const val COMMENT_USER_ID = "userid"

        const val FAVORITE_TABLE = "favorite"
        const val FAVORITE_ID = "id"
        const val FAVORITE_RECIPE_ID = "recipeid"
        const val FAVORITE_USER_ID = "userid"

        val CREATE_COMMAND: Array<String> = arrayOf(
            "CREATE TABLE $USER_TABLE ("+
            "$USER_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$USER_NAME STRING NOT NULL)",

            "CREATE TABLE $CATEGORY_TABLE ("+
            "$CATEGORY_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$CATEGORY_NAME STRING NOT NULL)",

            "CREATE TABLE $RECIPE_TABLE ("+
            "$RECIPE_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$RECIPE_AUTHOR_ID INTEGER NOT NULL,"+
            "$RECIPE_CATEGORY_ID INTEGER NOT NULL,"+
            "$RECIPE_NAME STRING NOT NULL,"+
            "$RECIPE_INGREDIENTS STRING,"+
            "$RECIPE_COOKING STRING)",

            "CREATE TABLE $IMAGE_TABLE ("+
            "$IMAGE_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$IMAGE_RECIPE_ID INTEGER NOT NULL,"+
            "$IMAGE_NUM INTEGER NOT NULL,"+
            "$IMAGE_IMAGE STRING)",

            "CREATE TABLE $COMMENT_TABLE ("+
            "$COMMENT_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$COMMENT_RECIPE_ID INTEGER NOT NULL,"+
            "$COMMENT_RECIPE_GRADE INTEGER,"+
            "$COMMENT_TEXT STRING,"+
            "$COMMENT_USER_ID INTEGER NOT NULL)",

            "CREATE TABLE $FAVORITE_TABLE ("+
            "$FAVORITE_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
            "$FAVORITE_RECIPE_ID INTEGER NOT NULL,"+
            "$FAVORITE_USER_ID INTEGER NOT NULL)",

            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Европейская')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Азиатская')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Паназиатская')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Восточная')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Американская')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Русская')",
            "INSERT INTO $CATEGORY_TABLE($CATEGORY_NAME) VALUES('Средиземноморская')",
        )
    }

    fun getUserId(userName: String): Long {
        var userId: Long = 0
        if (userName.isEmpty()) {
            return userId
        }
        db.query(
            USER_TABLE,
            arrayOf(USER_ID),
            "$USER_NAME = ?",
            arrayOf(userName),
            null,
            null,
            USER_ID
        ).use {
            if (it.moveToFirst()) {
                userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
            }
        }
        if (userId == 0L) {
            val values = ContentValues().apply {
                put(USER_NAME, userName)
            }
            db.insert(USER_TABLE, null, values)
            userId = getMaxId(USER_TABLE)
        }
        return userId
    }

    private fun map(cursor: Cursor): Recipe {
        with (cursor) {
            return Recipe(
                id = getLong(getColumnIndexOrThrow(RECIPE_ID)),
                authorId = getLong(getColumnIndexOrThrow(RECIPE_AUTHOR_ID)),
                authorName = getString(getColumnIndexOrThrow("AuthorName")),
                categoryId = getInt(getColumnIndexOrThrow(RECIPE_CATEGORY_ID)),
                category = getString(getColumnIndexOrThrow("CategoryName")),
                name = getString(getColumnIndexOrThrow(RECIPE_NAME)),
                ingredients = getString(getColumnIndexOrThrow(RECIPE_INGREDIENTS)),
                cookingProcess = getString(getColumnIndexOrThrow(RECIPE_COOKING)),
                totalGrade = getInt(getColumnIndexOrThrow("RecipeGrade")),
                myGrade = getInt(getColumnIndexOrThrow("MyGrade")),
                commentsCount = getLong(getColumnIndexOrThrow("CommentsCount")),
                isFavorite = getInt(getColumnIndexOrThrow("IsFavorite")) != 0,
                imageId  = getLong(getColumnIndexOrThrow("ImageId")),
            )
        }
    }

    fun getAll():List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val queryResulty: Array<String> = emptyArray()
        val sql ="SELECT $RECIPE_TABLE.$RECIPE_ID, $RECIPE_AUTHOR_ID, $USER_TABLE.$USER_NAME as AuthorName, " +
                 "$RECIPE_CATEGORY_ID, IFNULL($CATEGORY_TABLE.$CATEGORY_NAME,'') as CategoryName, " +
                 "$RECIPE_TABLE.$RECIPE_NAME, $RECIPE_INGREDIENTS, $RECIPE_COOKING, " +
                 "(SELECT CASE WHEN COUNT($COMMENT_RECIPE_GRADE)>0 THEN SUM($COMMENT_RECIPE_GRADE)*100/COUNT($COMMENT_RECIPE_GRADE) ELSE 0 END from $COMMENT_TABLE WHERE IFNULL($COMMENT_TABLE.$COMMENT_RECIPE_GRADE,0)<>0 AND $COMMENT_TABLE.$COMMENT_RECIPE_ID=$RECIPE_TABLE.$RECIPE_ID) as RecipeGrade, "+
                 "(SELECT IFNULL(SUM($COMMENT_RECIPE_GRADE),0) FROM $COMMENT_TABLE WHERE $COMMENT_TABLE.$COMMENT_RECIPE_ID=$RECIPE_TABLE.$RECIPE_ID AND $COMMENT_TABLE.$COMMENT_USER_ID="+currentUser.id.toString()+") as MyGrade, "+
                 "(SELECT COUNT(*) FROM $COMMENT_TABLE WHERE $COMMENT_TABLE.$COMMENT_RECIPE_ID=$RECIPE_TABLE.$RECIPE_ID AND $COMMENT_TEXT IS NOT NULL) as CommentsCount, "+
                 "CASE WHEN $FAVORITE_TABLE.$FAVORITE_ID IS NULL THEN 0 ELSE 1 END as IsFavorite, " +
                 "IFNULL($IMAGE_TABLE.$IMAGE_ID,0) as ImageId FROM $RECIPE_TABLE " +
                 "LEFT JOIN $USER_TABLE on $RECIPE_TABLE.$RECIPE_AUTHOR_ID = $USER_TABLE.$USER_ID " +
                 "LEFT JOIN $CATEGORY_TABLE on $RECIPE_TABLE.$RECIPE_CATEGORY_ID = $CATEGORY_TABLE.$CATEGORY_ID " +
                 "LEFT JOIN $IMAGE_TABLE on $RECIPE_TABLE.$RECIPE_ID = $IMAGE_TABLE.$IMAGE_RECIPE_ID AND $IMAGE_TABLE.$IMAGE_NUM=1 "+
                 "LEFT JOIN $FAVORITE_TABLE on $RECIPE_TABLE.$RECIPE_ID=$FAVORITE_TABLE.$FAVORITE_RECIPE_ID AND $FAVORITE_TABLE.$FAVORITE_USER_ID="+currentUser.id.toString()+" "+
                 "ORDER BY $RECIPE_TABLE.$RECIPE_ID DESC"
        db.rawQuery(sql,queryResulty).use {
            if (it.moveToFirst()) {
                do {
                    recipes.add(map(it))
                } while (it.moveToNext())
            }
        }
        return recipes
    }

    private fun getMaxId(table: String): Long {
        db.query(
            table,
            arrayOf("MAX(id)"),
            null,
            null,
            null,
            null,
            null).use {
            if ((it.count>0) && (it.moveToFirst())) {
                return it.getLongOrNull(0) ?: 0
            } else {
                return 0
            }
        }
    }

    fun getCategories(): ArrayList<String> {
        val categorys = arrayListOf<String>()
        db.query(
            CATEGORY_TABLE,
            arrayOf(CATEGORY_NAME),
            null,
            null,
            null,
            null,
            null).use {
            with(it) {
                if (moveToFirst()) {
                    do {
                        categorys.add(getString(getColumnIndexOrThrow(CATEGORY_NAME)))
                    } while (moveToNext())
                }
            }
        }
        return categorys
    }

    fun new(recipe:Recipe, bitmap:Bitmap?):Recipe {
        val values = ContentValues().apply {
            put(RECIPE_AUTHOR_ID, currentUser.id)
            put(RECIPE_CATEGORY_ID, recipe.categoryId)
            put(RECIPE_NAME, recipe.name)
            put(RECIPE_INGREDIENTS, recipe.ingredients)
            put(RECIPE_COOKING, recipe.cookingProcess)
        }
        db.insert(RECIPE_TABLE, null, values)
        val recipeId = getMaxId(RECIPE_TABLE)
        val imageId = addImage(recipeId, bitmap)
        return recipe.copy(id = recipeId, imageId = imageId)
    }

    fun delete(recipeId: Long) {
        val id = arrayOf(recipeId.toString())
        db.delete(RECIPE_TABLE, "$RECIPE_ID=?", id)
        db.delete(COMMENT_TABLE, "$COMMENT_RECIPE_ID =?", id)
        db.delete(IMAGE_TABLE, "$IMAGE_RECIPE_ID =?", id)
        db.delete(FAVORITE_TABLE, "$FAVORITE_RECIPE_ID =?", id)
    }

    fun edit(recipe:Recipe, bitmap:Bitmap?): Recipe {
        val values = ContentValues().apply {
            put(RECIPE_CATEGORY_ID, recipe.categoryId)
            put(RECIPE_NAME, recipe.name)
            put(RECIPE_INGREDIENTS, recipe.ingredients)
            put(RECIPE_COOKING, recipe.cookingProcess)
        }
        db.update(RECIPE_TABLE, values, "$RECIPE_ID=?", arrayOf(recipe.id.toString()))

        var imageId = recipe.imageId

        if (bitmap == null) {
            deleteImage(imageId)
            imageId = 0L
        } else {
            if (imageId != 0L) {
                updateImage(imageId, bitmap)
            } else {
                imageId = addImage(recipe.id, bitmap)
            }
        }
        return recipe.copy(imageId = imageId)
    }

    fun getComments(recipeId: Long): ArrayList<String> {
        val coments = ArrayList<String>()
        val queryResulty: Array<String> = emptyArray()
        val sql ="SELECT $COMMENT_TEXT, $USER_TABLE.$USER_NAME "+
                 "FROM $COMMENT_TABLE LEFT JOIN $USER_TABLE on $COMMENT_TABLE.$COMMENT_USER_ID = $USER_TABLE.$USER_ID "+
                 "WHERE $COMMENT_TABLE.$COMMENT_RECIPE_ID="+recipeId.toString()+" AND $COMMENT_TABLE.$COMMENT_TEXT IS NOT NULL"
        db.rawQuery(sql,queryResulty).use {
            if (it.moveToFirst()) {
                do {
                    coments.add(
                        it.getString(it.getColumnIndexOrThrow(COMMENT_TEXT))+
                        "("+it.getString(it.getColumnIndexOrThrow(USER_NAME))+")"
                    )
                } while (it.moveToNext())
            }
        }
        return coments
    }

    fun addComment(recipeId: Long, text:String) {
        val values = ContentValues().apply {
            put(COMMENT_RECIPE_ID, recipeId)
            put(COMMENT_TEXT, text)
            put(COMMENT_USER_ID, currentUser.id)
        }
        db.insert(COMMENT_TABLE, null, values)
    }

    fun deleteComment(commentId: Long) {
        db.delete(COMMENT_TABLE, "$COMMENT_ID=?", arrayOf(commentId.toString()))
    }

    fun deleteComment(commentText: String) {
        db.delete(COMMENT_TABLE, "$COMMENT_TEXT = ? AND $COMMENT_USER_ID = ?", arrayOf(commentText, currentUser.id.toString()))
    }

    /*
    задел на будущее, чтобы можно было хранить несколко картинок в рецепте
    fun getImages(recipeId: Long): ArrayList<Long> {
        val imageList = ArrayList<Long>()

        db.query(IMAGE_TABLE,
            arrayOf("$IMAGE_ID"),
            "$IMAGE_RECIPE_ID="+recipeId.toString(),
            null,
            null,
            null,
            "$IMAGE_NUM",
            null).use {
            if (it.moveToFirst()) {
                do {
                    imageList.add(it.getLong(it.getColumnIndexOrThrow(IMAGE_RECIPE_ID)))
                } while (it.moveToNext())
            }
        }
        return imageList
    }
    */

    private fun addImage(recipeId: Long, bitmap: Bitmap?):Long {
        if (bitmap != null) {
            val bitmapString =  bitmap2String(bitmap)
            val values = ContentValues().apply {
                put(IMAGE_RECIPE_ID, recipeId)
                put(IMAGE_NUM, 1)
                put(IMAGE_IMAGE, bitmapString)
            }
            db.insert(IMAGE_TABLE, null, values)
            return getMaxId(IMAGE_TABLE)
        } else {
            return 0L
        }
    }

    private fun updateImage(imageId: Long, bitmap: Bitmap) {
        val bitmapString =  bitmap2String(bitmap)
        val values = ContentValues().apply {
            put(IMAGE_ID, imageId)
            put(IMAGE_IMAGE, bitmapString)
        }
        db.insert(IMAGE_TABLE, null, values)
    }

    fun getImage(imageId:Long): Bitmap? {
        db.query(IMAGE_TABLE,
                 arrayOf(IMAGE_IMAGE),
                "$IMAGE_ID=$imageId",
                null,
                null,
                null,
                null,
                null).use {
            if (it.moveToFirst()) {
                val decodedString = Base64.decode(it.getString(it.getColumnIndexOrThrow(IMAGE_IMAGE)), Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } else
                return null
        }
    }

    private fun deleteImage(imageId: Long) {
        db.delete(IMAGE_TABLE, "$IMAGE_ID=?", arrayOf(imageId.toString()))
    }

    fun setFavorite(recipeId: Long, isFavorite: Boolean) {
        if (isFavorite) {
            val values = ContentValues().apply {
                put(FAVORITE_RECIPE_ID, recipeId)
                put(FAVORITE_USER_ID, currentUser.id)
            }
            db.insert(FAVORITE_TABLE, null, values)
        } else {
            db.delete(FAVORITE_TABLE, "$FAVORITE_RECIPE_ID=?", arrayOf(recipeId.toString()))
        }
    }

    fun updateGrade(recipeId: Long, grade: Int) {
        var gradeId = 0
        var count = 0
        db.query(COMMENT_TABLE,
                 arrayOf(COMMENT_ID),
                "$COMMENT_RECIPE_ID="+recipeId.toString()+" AND $COMMENT_USER_ID="+ currentUser.id+" AND $COMMENT_RECIPE_GRADE IS NOT NULL",
                null,
                null,
                null,
                null,
                null).use {
            count = it.count
            if ((count>0) && (it.moveToFirst())) {
                gradeId = it.getIntOrNull(0) ?: 0
                if (count > 1) { gradeId = 0 }
            }
        }
        if ((grade == 0) || (count > 1)) {
            db.delete(COMMENT_TABLE, "$COMMENT_RECIPE_ID="+recipeId.toString()+" AND $COMMENT_USER_ID="+ currentUser.id+" AND $COMMENT_RECIPE_GRADE IS NOT NULL", null)
        }
        if (grade > 0) {
            if (gradeId > 0) {
                val values = ContentValues().apply {
                    put(COMMENT_RECIPE_GRADE, grade)
                }
                db.update(COMMENT_TABLE, values,
                    "$COMMENT_ID = $gradeId",
                    null)
            } else {
                val values = ContentValues().apply {
                    put(COMMENT_RECIPE_ID, recipeId)
                    put(COMMENT_USER_ID, currentUser.id)
                    put(COMMENT_RECIPE_GRADE, grade)
                }
                db.insert(COMMENT_TABLE, null, values)
            }
        }
    }

    fun getTotalGrade(recipeId: Long):Int {
        val queryResulty: Array<String> = emptyArray()
        val sql =
            "SELECT CASE WHEN COUNT($COMMENT_RECIPE_GRADE)>0 THEN SUM($COMMENT_RECIPE_GRADE)*100/COUNT($COMMENT_RECIPE_GRADE) ELSE 0 END from $COMMENT_TABLE WHERE IFNULL($COMMENT_TABLE.$COMMENT_RECIPE_GRADE,0)<>0 AND $COMMENT_TABLE.$COMMENT_RECIPE_ID=$recipeId"
        db.rawQuery(sql,queryResulty).use {
            if (it.moveToFirst()) {
                return it.getIntOrNull(0) ?: 0
            } else {
                return 0
            }
        }
    }

    fun getUsers(): ArrayList<String> {
        val users = arrayListOf<String>()
        db.query(
            USER_TABLE,
            arrayOf(USER_NAME),
            null,
            null,
            null,
            null,
            null).use {
            with(it) {
                if (moveToFirst()) {
                    do {
                        users.add(getString(getColumnIndexOrThrow(USER_NAME)))
                    } while (moveToNext())
                }
            }
        }
        return users
    }
}