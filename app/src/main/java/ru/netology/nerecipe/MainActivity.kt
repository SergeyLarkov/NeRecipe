package ru.netology.nerecipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import ru.netology.nerecipe.data.User
import ru.netology.nerecipe.data.currentUser
import ru.netology.nerecipe.viewmodel.RecipeViewModel
import ru.netology.nerecipe.utils.hideKeyboard

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: RecipeViewModel by viewModels()

        setContentView(R.layout.activity_main)
        // val binding = ActivityMainBinding.inflate(layoutInflater)
        // binding.startButton.setOnClickListener {
        // не работает хз почему...
        val b = findViewById<TextView>(R.id.startButton)
        b.setOnClickListener {
            //val name = binding.editTextTextPersonName.text.toString()
            val editTextView = findViewById<EditText>(R.id.editTextTextPersonName)
            val name = editTextView.text.toString()
            if (name.isNotBlank()) {
                hideKeyboard(editTextView)
                editTextView.setText("")
                currentUser = User(viewModel.getUserid(name), name)
                viewModel.setValue()
                if (savedInstanceState == null) {
                    with(supportFragmentManager.beginTransaction()) {
                        add(R.id.fragmentContainerView, RecipeListFragment())
                        addToBackStack("listFragment")
                        commit()
                    }
                }
            }
        }
    }
}