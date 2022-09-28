package ru.netology.nerecipe

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.nerecipe.databinding.FragmentRecipeFilterBinding
import ru.netology.nerecipe.viewmodel.RecipeViewModel


class RecipeFilterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentRecipeFilterBinding.inflate(inflater,container,false)

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireActivity)
        viewModel.filterValues.clearValues()

        val categories = viewModel.getCategories()
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_checked, categories)
        binding.categoryList.adapter = categoryAdapter
        for (i in 0 until binding.categoryList.count) {
            binding.categoryList.setItemChecked(i, true)
        }

        val users = arrayListOf("") + viewModel.getUsers()
        val userAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, users)
        userAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.userSpinner.adapter = userAdapter
        binding.userSpinner.prompt = "Пользователь"

        binding.categoryList.setOnItemClickListener(OnItemClickListener { _, view, _, _ ->
            val v = view as CheckedTextView
            if ((!v.isChecked) && (binding.categoryList.checkedItemCount == 0)) {
                v.isChecked = true
            }
        })

        binding.okButton.setOnClickListener {
            viewModel.filterValues.categories
            binding.categoryList.forEach {
                if ((it as CheckedTextView).isChecked) {
                    viewModel.filterValues.categories.add(
                        categories.indexOf(it.text.toString()) + 1
                    )
                }
            }
            viewModel.filterValues.userid =
                viewModel.getUserid(binding.userSpinner.selectedItem.toString())
            viewModel.filterValues.grade = binding.gradeSeekBar.progress * 100
            viewModel.filterValues.withComments = binding.commentsCheckBox.isChecked
            activity?.supportFragmentManager?.popBackStack()
            viewModel.applyFilterEvent.call()
        }

        return binding.root
    }

}