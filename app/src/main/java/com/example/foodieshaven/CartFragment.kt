package com.example.foodieshaven

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<Cart>()
    private lateinit var database: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private lateinit var noItemsTextView: TextView
    private lateinit var totalAmountTextView: TextView
    private lateinit var checkoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(context, "Please log in to view your cart", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, LoginFragment())
                .addToBackStack(null)
                .commit()
            return
        }

        val userId = user.uid
        val cartRecyclerView: RecyclerView = view.findViewById(R.id.cartRecyclerView)
        noItemsTextView = view.findViewById(R.id.noItemsTextView)
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView)
        checkoutButton = view.findViewById(R.id.checkoutButton)

        cartRecyclerView.layoutManager = LinearLayoutManager(context)

        database = FirebaseDatabase.getInstance().reference.child("cart")
        userDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        cartAdapter = CartAdapter(cartList, onQuantityChanged = { cart ->
            updateCart(cart)
        }, onRemoveClicked = { cart ->
            removeFromCart(cart)
        })
        cartRecyclerView.adapter = cartAdapter

        fetchCartItems(userId)

        checkoutButton.setOnClickListener {
            if (cartList.isNotEmpty()) {
                fetchUserDetailsAndPlaceOrder(userId)
            } else {
                Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCartItems(userId: String) {
        val cartRef = database.child(userId)

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                var totalAmount = 0.0
                for (cartSnapshot in snapshot.children) {
                    val cartItem = cartSnapshot.getValue(Cart::class.java)
                    cartItem?.let {
                        cartList.add(it)
                        totalAmount += it.price * it.quantity
                    }
                }
                cartAdapter.notifyDataSetChanged()
                noItemsTextView.visibility = if (cartList.isEmpty()) View.VISIBLE else View.GONE
                totalAmountTextView.text = String.format("Total: Rs. %.2f", totalAmount)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cart items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCart(cart: Cart) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = database.child(userId)

        cartRef.child(cart.id).setValue(cart)
    }

    private fun removeFromCart(cart: Cart) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = database.child(userId)

        cartRef.child(cart.id).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserDetailsAndPlaceOrder(userId: String) {
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val phone = snapshot.child("mobile").getValue(String::class.java) ?: "No Phone"
                val address = snapshot.child("address").getValue(String::class.java) ?: "No Address"

                placeOrder(userId, name, phone, address)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun placeOrder(userId: String, customerName: String, customerPhone: String, customerAddress: String) {
        val ordersRef = FirebaseDatabase.getInstance().reference.child("orders").child(userId)
        val paymentMethod = "Cash on Delivery"

        for (cartItem in cartList) {
            val newOrderRef = ordersRef.push()
            val orderData = mapOf(
                "id" to newOrderRef.key,
                "foodName" to cartItem.title,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "customerAddress" to customerAddress,
                "imageName" to cartItem.imageName,
                "quantity" to cartItem.quantity,
                "totalPrice" to cartItem.price * cartItem.quantity,
                "paymentMethod" to paymentMethod,
                "timestamp" to System.currentTimeMillis(),
                "status" to "Pending"  // Default status when placing an order
            )

            newOrderRef.setValue(orderData).addOnFailureListener {
                Toast.makeText(context, "Failed to place order", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear cart after order placement
        database.child(userId).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to clear cart", Toast.LENGTH_SHORT).show()
        }
    }
}
