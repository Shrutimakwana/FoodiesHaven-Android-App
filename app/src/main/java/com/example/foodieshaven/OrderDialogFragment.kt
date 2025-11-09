package com.example.foodieshaven

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderDialogFragment(private val food: Food) : DialogFragment() {

    private lateinit var customerName: EditText
    private lateinit var customerPhone: EditText
    private lateinit var customerAddress: EditText
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_order_form, null)

        // UI References
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodTitle: TextView = view.findViewById(R.id.foodTitle)
        val foodQuantity: EditText = view.findViewById(R.id.foodQuantity)
        val totalPriceText: TextView = view.findViewById(R.id.totalPrice)
        customerName = view.findViewById(R.id.customerName)
        customerPhone = view.findViewById(R.id.customerPhone)
        customerAddress = view.findViewById(R.id.customerAddress)
        val paymentMethod: Spinner = view.findViewById(R.id.paymentMethod)
        val confirmButton: Button = view.findViewById(R.id.confirmOrderButton)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Load user data
        loadUserData()

        // Set Food Title
        foodTitle.text = food.title

        // Load Image from Drawable
        val resourceId = requireContext().resources.getIdentifier(
            food.imageName, "drawable", requireContext().packageName
        )
        foodImage.setImageResource(if (resourceId != 0) resourceId else R.drawable.ice_cream)

        // Set Default Quantity
        foodQuantity.setText("1")

        // Populate Payment Methods Spinner
        val paymentOptions = arrayOf("Cash on Delivery", "UPI Payment", "Credit/Debit Card")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paymentOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentMethod.adapter = adapter

        // Update Total Price when Quantity Changes
        fun updateTotalPrice() {
            val quantity = foodQuantity.text.toString().toIntOrNull() ?: 1
            val totalPrice = quantity * food.price
            totalPriceText.text = "Total Price: Rs. $totalPrice"
        }
        updateTotalPrice()

        foodQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateTotalPrice() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Confirm Order Button Click
        confirmButton.setOnClickListener {
            val name = customerName.text.toString().trim()
            val phone = customerPhone.text.toString().trim()
            val address = customerAddress.text.toString().trim()
            val quantity = foodQuantity.text.toString().toIntOrNull() ?: 1
            val selectedPaymentMethod = paymentMethod.selectedItem.toString()
            val totalPrice = quantity * food.price

            // Validate Fields
            when {
                name.isEmpty() -> {
                    customerName.error = "Enter your name"
                    return@setOnClickListener
                }
                phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches() -> {
                    customerPhone.error = "Enter a valid phone number"
                    return@setOnClickListener
                }
                address.isEmpty() -> {
                    customerAddress.error = "Enter a delivery address"
                    return@setOnClickListener
                }
            }

            // Store Order in Firebase under user ID
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val orderRef = FirebaseDatabase.getInstance().reference.child("orders").child(userId).push()

            val orderData = mapOf(
                "id" to orderRef.key,
                "foodName" to food.title,
                "customerName" to name,
                "customerPhone" to phone,
                "customerAddress" to address,
                "imageName" to food.imageName,
                "quantity" to quantity,
                "totalPrice" to totalPrice,
                "paymentMethod" to selectedPaymentMethod,
                "status" to "Pending"  // Default status when placing an order
            )

            orderRef.setValue(orderData).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Order Failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setView(view)
        return builder.create()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("OrderDialog", "User not logged in")
            return
        }

        val userId = user.uid
        Log.d("OrderDialog", "Fetching user data for ID: $userId")

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val phone = snapshot.child("mobile").getValue(String::class.java) ?: ""
                    val address = snapshot.child("address").getValue(String::class.java) ?: ""

                    Log.d("OrderDialog", "User Data Loaded: Name=$name, Phone=$phone, Address=$address")

                    // Ensure UI is updated on the main thread
                    Handler(Looper.getMainLooper()).post {
                        customerName.setText(name)
                        customerPhone.setText(phone)
                        customerAddress.setText(address)
                    }
                } else {
                    Log.e("OrderDialog", "User data not found in Firebase")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderDialog", "Failed to load user data: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
