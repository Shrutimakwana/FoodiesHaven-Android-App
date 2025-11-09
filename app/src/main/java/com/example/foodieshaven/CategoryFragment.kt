package com.example.foodieshaven

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class CategoryFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryList = mutableListOf<Category>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryRecyclerView: RecyclerView = view.findViewById(R.id.categoryRecyclerView)

        // Set GridLayoutManager with 2 columns
        categoryRecyclerView.layoutManager = GridLayoutManager(context, 2)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("categories")

        // Fetch data from Firebase
        fetchCategoriesFromFirebase()

        // Set the adapter with click listener
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            navigateToFoodFragment(category)
        }
        categoryRecyclerView.adapter = categoryAdapter
    }

    private fun fetchCategoriesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No categories available", Toast.LENGTH_SHORT).show()
                    return
                }
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let {
                        categoryList.add(it)
                    }
                }
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryFragment", "Database Error: ${error.message}")
                Toast.makeText(context, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToFoodFragment(category: Category) {
        Log.d("CategoryFragment", "Navigating to FoodFragment with category: ${category.title}")

        val foodFragment = FoodFragment().apply {
            arguments = Bundle().apply {
                putString("categoryId", category.id)  // Pass category.id as categoryId
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.frame_container, foodFragment, FoodFragment::class.java.simpleName)
            .addToBackStack(null)
            .commit()

        Log.d("CategoryFragment", "FoodFragment committed successfully.")
    }
}
