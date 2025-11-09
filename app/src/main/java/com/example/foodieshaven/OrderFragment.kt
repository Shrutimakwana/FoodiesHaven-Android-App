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

class OrderFragment : Fragment() {

    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<Order>()
    private lateinit var database: DatabaseReference
    private lateinit var noOrdersTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ordersRecyclerView: RecyclerView = view.findViewById(R.id.ordersRecyclerView)
        noOrdersTextView = view.findViewById(R.id.noOrdersTextView)

        ordersRecyclerView.layoutManager = LinearLayoutManager(context)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "Please log in to view orders", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid // Get current user ID
        database = FirebaseDatabase.getInstance().reference.child("orders").child(userId)

        // Initialize the adapter with cancel functionality
        orderAdapter = OrderAdapter(orderList) { order ->
            cancelOrder(order)
        }
        ordersRecyclerView.adapter = orderAdapter

        fetchOrders(userId)
    }

    private fun fetchOrders(userId: String) { // Accept userId as parameter
        val userOrdersRef = FirebaseDatabase.getInstance().reference.child("orders").child(userId)

        userOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderItem = orderSnapshot.getValue(Order::class.java)
                    orderItem?.let { orderList.add(it) }
                }
                orderAdapter.notifyDataSetChanged()
                noOrdersTextView.visibility = if (orderList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cancelOrder(order: Order) {
        val id = order.id // Ensure this is unique for each order

        if (id.isEmpty()) {
            Toast.makeText(context, "Invalid order ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the order's status is Pending, Failed, or On Hold
        if (order.status == "Pending" || order.status == "Failed" || order.status == "On Hold") {
            database.child(id).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Order Canceled Successfully", Toast.LENGTH_SHORT).show()
                    orderList.remove(order) // Remove only the selected order
                    orderAdapter.notifyDataSetChanged()
                    noOrdersTextView.visibility = if (orderList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(context, "Failed to cancel order", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Show a message that the order cannot be canceled
            Toast.makeText(context, "This order cannot be canceled", Toast.LENGTH_SHORT).show()
        }
    }
}
