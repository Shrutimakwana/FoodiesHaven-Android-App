package com.example.foodieshaven

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var foodAdapter: FoodAdapter
    private var foodList = mutableListOf<Food>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchInput = view.findViewById(R.id.searchInput)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)

        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodAdapter = FoodAdapter(foodList, ::onOrderClick, ::onAddToCartClick)
        searchRecyclerView.adapter = foodAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("foods")

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFood(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun searchFood(query: String) {
        val searchQuery = query.lowercase().trim()

        if (searchQuery.isEmpty()) {
            foodList.clear()
            foodAdapter.notifyDataSetChanged()
            noResultsTextView.visibility = View.GONE
            return
        }

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<Food>()

                for (foodSnapshot in snapshot.children) {
                    val food = foodSnapshot.getValue(Food::class.java)
                    if (food != null) {
                        val categoryMatch = food.categoryId.lowercase().contains(searchQuery)
                        val titleMatch = food.title.lowercase().contains(searchQuery)
                        val descriptionMatch = food.description.lowercase().contains(searchQuery)

                        if (categoryMatch || titleMatch || descriptionMatch) {
                            filteredList.add(food)
                        }
                    }
                }

                foodList.clear()
                foodList.addAll(filteredList)
                foodAdapter.notifyDataSetChanged()

                noResultsTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onOrderClick(food: Food) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to place an order", Toast.LENGTH_SHORT).show()
            navigateToLoginFragment()
        } else {
            showOrderDialog(food)
        }
    }

    private fun onAddToCartClick(food: Food) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to add items to cart", Toast.LENGTH_SHORT).show()
            navigateToLoginFragment()
        } else {
            addToCart(food, user.uid)
        }
    }

    private fun addToCart(food: Food, userId: String) {
        val cartDatabase = FirebaseDatabase.getInstance().getReference("cart")
        val cartRef = cartDatabase.child(userId).child(food.id)

        val cartItem = mapOf(
            "id" to food.id,
            "title" to food.title,
            "price" to food.price,
            "imageName" to food.imageName
        )

        cartRef.setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${food.title} added to cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add ${food.title}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showOrderDialog(food: Food) {
        val dialog = OrderDialogFragment(food)
        dialog.show(childFragmentManager, "OrderDialog")
    }

    private fun navigateToLoginFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, LoginFragment())
            .addToBackStack(null)
            .commit()
    }
}
