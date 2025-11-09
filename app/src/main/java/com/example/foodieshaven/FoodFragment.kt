package com.example.foodieshaven

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FoodFragment : Fragment() {

    private lateinit var foodAdapter: FoodAdapter
    private val foodList = mutableListOf<Food>()
    private lateinit var database: DatabaseReference
    private lateinit var cartDatabase: DatabaseReference
    private lateinit var noFoodTextView: TextView
    private var categoryId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foodRecyclerView: RecyclerView = view.findViewById(R.id.foodRecyclerView)
        noFoodTextView = view.findViewById(R.id.noFoodTextView)

        foodRecyclerView.layoutManager = LinearLayoutManager(context)

        categoryId = arguments?.getString("categoryId") ?: ""
        database = FirebaseDatabase.getInstance().reference.child("foods")
        cartDatabase = FirebaseDatabase.getInstance().reference.child("cart")

        fetchFoodFromFirebase()

        foodAdapter = FoodAdapter(
            foodList,
            onOrderClick = { selectedFood -> checkAndOrderNow(selectedFood) },
            onAddToCartClick = { selectedFood -> checkAndAddToCart(selectedFood) }
        )

        foodRecyclerView.adapter = foodAdapter
    }

    private fun fetchFoodFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                for (foodSnapshot in snapshot.children) {
                    val food = foodSnapshot.getValue(Food::class.java)
                    food?.let {
                        if (it.categoryId == categoryId) {
                            foodList.add(it)
                        }
                    }
                }
                foodAdapter.notifyDataSetChanged()
                noFoodTextView.visibility = if (foodList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load food items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAndAddToCart(food: Food) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "Please log in to add items to cart", Toast.LENGTH_SHORT).show()
            navigateToLoginFragment()
        } else {
            addToCart(food, user.uid)
        }
    }

    private fun addToCart(food: Food, userId: String) {
        val cartRef = cartDatabase.child(userId).child(food.id)

        val cartItem = mapOf(
            "id" to food.id,
            "title" to food.title,
            "price" to food.price,
            "imageName" to food.imageName
        )

        cartRef.setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(context, "${food.title} added to cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add ${food.title}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAndOrderNow(food: Food) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "Please log in to place an order", Toast.LENGTH_SHORT).show()
            navigateToLoginFragment()
        } else {
            showOrderDialog(food)
        }
    }

    private fun showOrderDialog(food: Food) {
        val dialog = OrderDialogFragment(food)
        dialog.show(childFragmentManager, "OrderDialog")
    }

    private fun navigateToLoginFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, LoginFragment()) // Change 'R.id.frame_container' as per your layout
            .addToBackStack(null)
            .commit()
    }
}
