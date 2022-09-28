package ru.netology.nerecipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import ru.netology.nerecipe.data.Recipe
import ru.netology.nerecipe.data.currentUser
import ru.netology.nerecipe.databinding.FragmentRecipeListBinding
import ru.netology.nerecipe.viewmodel.RecipeAdapter
import ru.netology.nerecipe.viewmodel.RecipeViewModel
import ru.netology.nerecipe.utils.hideKeyboard

class RecipeListFragment : Fragment() {

    val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val adapter = RecipeAdapter(viewModel)

        val binding = FragmentRecipeListBinding.inflate(inflater,container,false)
        binding.RecipeListView.adapter = adapter

        if (viewModel.filterValues.favoriteSelected) {
            binding.bottomTabs.selectTab(binding.bottomTabs.getTabAt(1))
        }

        binding.addRecipeButton.setOnClickListener{ editRecipe(null) }

        binding.filterButton.setOnClickListener{
            val filterFragment = RecipeFilterFragment()

            val fm = activity?.supportFragmentManager
            if (fm != null) {
                with (fm.beginTransaction()) {
                    replace(R.id.fragmentContainerView,filterFragment,"filterFragment")
                    addToBackStack("filterFragment")
                    commit()
                }
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { recipes ->
            adapter.list = recipes
            if (recipes.isEmpty()) {
                binding.noRecipesImage.visibility = VISIBLE
                binding.noRecipesText.visibility = VISIBLE
            } else {
                binding.noRecipesImage.visibility = GONE
                binding.noRecipesText.visibility = GONE
            }
        }

        viewModel.editEvent.observe(viewLifecycleOwner) { recipe ->
            if (recipe.authorId != currentUser.id) {
                Snackbar.make(binding.root,
                    "Нельзя редактировать чужые рецепты",
                    LENGTH_SHORT).show()
            } else {
                editRecipe(recipe)
            }
        }

        viewModel.deleteEvent.observe(viewLifecycleOwner) { recipe ->
            if (recipe.authorId != currentUser.id) {
                Snackbar.make(binding.root,
                    "Нельзя удалять чужые рецепты",
                    LENGTH_SHORT).show()
            } else {
                deleteRecipe(recipe.id)
            }
        }

        viewModel.showEvent.observe(viewLifecycleOwner) { recipe ->
            val showFragment = RecipeShowFragment()

            if (recipe != null) {
                val bundle = Bundle()
                bundle.putLong(KEY_ID, recipe.id)
                showFragment.arguments = bundle
            }

            val fm = activity?.supportFragmentManager
            if (fm != null) {
                with (fm.beginTransaction()) {
                    replace(R.id.fragmentContainerView,showFragment,"showFragment")
                    addToBackStack("showFragment")
                    commit()
                }
            }
        }

        viewModel.applyFilterEvent.observe(viewLifecycleOwner) {
            viewModel.findRecipeByFilter()
        }

        binding.applySearchButton.setOnClickListener {
            val searchText = binding.searchRecipeText.text.toString()
            binding.searchRecipeText.clearFocus()
            hideKeyboard(it)
            if (searchText.isNotEmpty()) viewModel.findRecipeByName(searchText)
        }

        binding.bottomTabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            fun selectRecipies(position: Int?) {
                when (position) {
                    0 -> viewModel.getAllRecipes()
                    1 -> viewModel.getFavoriteRecipes()
                }
                viewModel.filterValues.favoriteSelected = (position == 1)
                binding.searchRecipeText.setText("")
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectRecipies(tab?.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectRecipies(tab?.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }
        })

        return binding.root
    }

    private fun deleteRecipe(recipeId: Long) = viewModel.deleteRecipe(recipeId)

    private fun editRecipe(recipe: Recipe?) {
        val editFragment = RecipeEditFragment()

        if (recipe != null) {
            val bundle = Bundle()
            bundle.putLong(KEY_ID, recipe.id)
            editFragment.arguments = bundle
        }

        val fm = activity?.supportFragmentManager
        if (fm != null) {
            with (fm.beginTransaction()) {
                replace(R.id.fragmentContainerView,editFragment,"editFragment")
                addToBackStack("editFragment")
                commit()
            }
        }
    }

    companion object KEYS {
        const val KEY_ID = "recipeId"
    }

}
