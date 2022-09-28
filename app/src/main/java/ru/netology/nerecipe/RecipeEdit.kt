package ru.netology.nerecipe

import android.R
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import ru.netology.nerecipe.RecipeListFragment.KEYS.KEY_ID
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.currentUser
import ru.netology.nerecipe.databinding.FragmentRecipeEditBinding
import ru.netology.nerecipe.viewmodel.RecipeViewModel
import java.io.IOException

class RecipeEditFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireActivity)

        val binding = FragmentRecipeEditBinding.inflate(inflater,container,false)

        val categories = viewModel.getCategories()

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.kitchenSpinner.adapter = adapter
        binding.kitchenSpinner.prompt = "Кухня"

        val bundle: Bundle?  = this.arguments

        val recipeId = bundle?.getLong(KEY_ID, 0)?:0
        val recipe = viewModel.getRecipe(id = recipeId)

        with(binding) {
            if (recipe != null) {
                recipeName.setText(recipe.name)
                kitchenSpinner.setSelection(recipe.categoryId - 1)
                ingredientsTextEdit.setText(recipe.ingredients)
                cookingTextEdit.setText(recipe.cookingProcess)
                addButton.setImageResource(ru.netology.nerecipe.R.drawable.ic_check_24)

                if (recipe.imageId !=0L ) {
                    val bitmap = viewModel.getImage(recipe.imageId)
                    if (bitmap != null) {
                        editImageView.setImageBitmap(bitmap)
                    }
                }
            } else {
                kitchenSpinner.setSelection(0)
                addButton.setImageResource(ru.netology.nerecipe.R.drawable.ic_add_24)
            }
        }

        binding.addImageButton.setOnClickListener {
            val photoPickerIntent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }
            startActivityForResult(photoPickerIntent, SELECT_IMAGE)
        }

        binding.delImageButton.setOnClickListener {
            binding.editImageView.setImageDrawable(null)
        }

        binding.addButton.setOnClickListener {
            if ((binding.recipeName.text.toString().isEmpty()) ||
                (binding.ingredientsTextEdit.text.toString().isEmpty()) ||
                (binding.cookingTextEdit.text.toString().isEmpty())) {
                Snackbar.make(
                    binding.root,
                    "Не заполнены все обязательные поля",
                    LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newRecipe = recipe?.copy(
                categoryId = binding.kitchenSpinner.selectedItemPosition + 1,
                category = binding.kitchenSpinner.selectedItem.toString(),
                name = binding.recipeName.text.toString(),
                ingredients = binding.ingredientsTextEdit.text.toString(),
                cookingProcess = binding.cookingTextEdit.text.toString()
            ) ?: Recipe(
                0,
                currentUser.id,
                currentUser.name,
                binding.kitchenSpinner.selectedItemPosition + 1,
                binding.kitchenSpinner.selectedItem.toString(),
                binding.recipeName.text.toString(),
                binding.ingredientsTextEdit.text.toString(),
                binding.cookingTextEdit.text.toString(),
                0,
                0,
                0,
                false,
                0
            )

            val drawable = binding.editImageView.drawable as BitmapDrawable?
            val bitmap = drawable?.bitmap

            viewModel.updateRecipe(newRecipe, bitmap)

            activity?.supportFragmentManager?.popBackStack()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((resultCode == RESULT_OK) && (requestCode == SELECT_IMAGE)) {
            val image: Uri? = data?.data
            if (image != null) {
                try {
                    val imageView = view?.findViewById<ImageView>(ru.netology.nerecipe.R.id.editImageView)
                    val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, image)
                    imageView?.setImageBitmap(bitmap)
                } catch (e: IOException) { }
            }
        }
    }

    companion object RESULTS {
        const val SELECT_IMAGE: Int = 1000
    }

}
