package ru.netology.nerecipe.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.RecipeEvents
import ru.netology.nerecipe.databinding.RecipeCardBinding

class RecipeViewHolder(private val binding: RecipeCardBinding,
                       private val recipeEvents: RecipeEvents): RecyclerView.ViewHolder(binding.root) {

    private lateinit var recipe: Recipe

    private val popupMenu = PopupMenu(itemView.context, binding.menuButton).apply {
        inflate(R.menu.option_recipe)
        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    recipeEvents.onDeleteClicked(recipe)
                    true
                }
                R.id.edit -> {
                    recipeEvents.onEditClicked(recipe)
                    true
                }
                else -> false
            }
        }
    }

    init {
        binding.menuButton.setOnClickListener{ popupMenu.show() }
        binding.favoriteButton.setOnClickListener { recipeEvents.onFavoriteClicked(recipe.id, binding.favoriteButton.isChecked) }
        binding.root.setOnClickListener {recipeEvents.onShowClicked(recipe) }
    }

    fun render(recipe: Recipe) {
        this.recipe = recipe
        with(binding) {
            recipeName.text = recipe.name
            kitchenName.text = recipe.category
            authorName.text = recipe.authorName
            if (recipe.totalGrade%100 == 0) {
                grade.text = (recipe.totalGrade/100).toString()+".00"
            } else {
                grade.text = (recipe.totalGrade/100).toString()+"."+(recipe.totalGrade%100).toString()
            }
            commentsCount.text = recipe.commentsCount.toString()
            favoriteButton.isChecked = recipe.isFavorite
            if (recipe.imageId != 0L) {
                val bitmap = recipeEvents.getImage(recipe.imageId)
                if (bitmap != null) {
                    cardImageView.setImageBitmap(bitmap)
                } else {
                    cardImageView.setImageResource(R.drawable.nophoto)
                }
            } else {
                cardImageView.setImageResource(R.drawable.nophoto)
            }
        }
    }
}

class RecipeAdapter(private val recipeEvents: RecipeEvents) : RecyclerView.Adapter<RecipeViewHolder>() {

    var list = emptyList<Recipe>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, recipeEvents)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = list[position]
        holder.render(recipe)
    }
}