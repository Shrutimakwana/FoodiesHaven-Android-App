package com.example.foodieshaven

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSubmit: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is logged in
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to submit a message", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return view
        }

        // Initialize UI elements
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etMessage = view.findViewById(R.id.etMessage)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Disable editing for email
        etEmail.isEnabled = false

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("contacts")

        // Fetch user details and pre-fill fields
        fetchUserDetails()

        btnSubmit.setOnClickListener {
            submitForm()
        }

        return view
    }

    private fun fetchUserDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    // Pre-fill the fields
                    etName.setText(name ?: "")
                    etEmail.setText(email ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitForm() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val message = etMessage.text.toString().trim()

        // Validations
        if (name.isEmpty()) {
            etName.error = "Name cannot be empty"
            etName.requestFocus()
            return
        } else if (name.length < 3) {
            etName.error = "Name must be at least 3 characters long"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email cannot be empty"
            etEmail.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return
        }

        if (message.isEmpty()) {
            etMessage.error = "Message cannot be empty"
            etMessage.requestFocus()
            return
        } else if (message.length < 10) {
            etMessage.error = "Message must be at least 10 characters long"
            etMessage.requestFocus()
            return
        }

        // Generate a unique ID for each contact message
        val contactId = database.push().key ?: ""

        // Create a Contact data object
        val contact = Contact(name, email, message)

        // Store the data in Firebase Realtime Database
        database.child(contactId).setValue(contact)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Message submitted successfully", Toast.LENGTH_SHORT).show()
                    clearFormFields()
                } else {
                    Toast.makeText(requireContext(), "Failed to submit message", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearFormFields() {
        etMessage.text.clear()
    }

    private fun navigateToLogin() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, LoginFragment())
            .addToBackStack(null)
            .commit()
    }
}
