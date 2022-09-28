package ru.netology.nerecipe

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import ru.netology.nerecipe.RecipeListFragment.KEYS.KEY_ID
import ru.netology.nerecipe.data.currentUser
import ru.netology.nerecipe.databinding.FragmentRecipeShowBinding
import ru.netology.nerecipe.viewmodel.RecipeViewModel
import ru.netology.nerecipe.utils.hideKeyboard

class RecipeShowFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ConstraintLayout {

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireActivity)

        val binding = FragmentRecipeShowBinding.inflate(inflater,container,false)

        val comments: ArrayList<String> = arrayListOf()

        val bundle: Bundle?  = this.arguments

        val recipeId = bundle?.getLong(KEY_ID, 0)?:0
        val recipe = viewModel.getRecipe(id = recipeId)

        with(binding) {
            if (recipe != null) {
                recipeName.text = recipe.name
                kitchenName.text = recipe.category
                authorName.text = recipe.authorName
                recipeIngredients.text = recipe.ingredients
                recipeCooking.text = recipe.cookingProcess
                comments.addAll(viewModel.getComments(recipe.id))
                seekBar.progress = recipe.myGrade
                favoriteButton.isChecked = recipe.isFavorite
                if (recipe.imageId !=0L ) {
                    val bitmap = viewModel.getImage(recipe.imageId)
                    if (bitmap != null) {
                        showImageView.setImageBitmap(bitmap)
                    }
                }
            } else {
                seekBar.progress = 0
            }
        }

        val commentsAdapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, comments)
        binding.commentsList.adapter = commentsAdapter

        binding.favoriteButton.setOnClickListener {
            if (recipe != null) {
                viewModel.onFavoriteClicked(recipe.id, binding.favoriteButton.isChecked)
            }
        }

        binding.applyCommentButton.setOnClickListener {
            val text = binding.commentTextEdit.text.toString()
            if (text.isNotEmpty()) {
                comments.add(0,text+" ("+currentUser.name+")")
                commentsAdapter.notifyDataSetChanged()
                if (recipe != null) { viewModel.addComment(recipe.id, text) }
            }
            binding.commentTextEdit.setText("")
            binding.commentTextEdit.clearFocus()
            hideKeyboard(it)
        }

        binding.commentsList.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { _, view, _, _ ->
            val comment = (view as TextView).text.toString()
            val namePos = comment.indexOf('(', 0)
            val nameLength = comment.indexOf(')', 0)
            val commentText = comment.substring(0,namePos)
            val name = comment.substring(namePos+1,nameLength)
            if (name == currentUser.name) {
                viewModel.deleteComment(commentText)
                commentsAdapter.remove(comment)
            } else {
                Snackbar.make(binding.root,
                    "Нельзя удалять чужой комментарий",
                    LENGTH_SHORT).show()
            }
            return@OnItemLongClickListener true
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (recipe != null) {
                    viewModel.updateGrade(recipe.id, progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        return binding.root
    }

}