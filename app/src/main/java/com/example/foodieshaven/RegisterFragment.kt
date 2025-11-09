package com.example.foodieshaven

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etMobile: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        etMobile = view.findViewById(R.id.etMobile)
        etAddress = view.findViewById(R.id.etAddress)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvLogin = view.findViewById(R.id.tvLogin)

        // Restrict mobile number input to 10 digits
        etMobile.filters = arrayOf(InputFilter.LengthFilter(10))

        // Add input validation for mobile number (must start with 6,7,8,9)
        etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1 && !s[0].toString().matches(Regex("[6789]"))) {
                    etMobile.error = "Mobile number must start with 6, 7, 8, or 9"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle registration button click
        btnRegister.setOnClickListener {
            registerUser()
        }

        // Handle login navigation
        tvLogin.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.frame_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun registerUser() {
        // Get user input
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        val address = etAddress.text.toString().trim()

        // Validate input
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }
        if (confirmPassword.isEmpty() || confirmPassword != password) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }
        if (mobile.isEmpty()) {
            etMobile.error = "Mobile number is required"
            etMobile.requestFocus()
            return
        }
        if (!mobile.matches(Regex("^[6789][0-9]{9}$"))) {
            etMobile.error = "Enter a valid 10-digit mobile number starting with 6, 7, 8, or 9"
            etMobile.requestFocus()
            return
        }
        if (address.isEmpty()) {
            etAddress.error = "Address is required"
            etAddress.requestFocus()
            return
        }
        if (address.length < 10) {
            etAddress.error = "Address should be at least 10 characters"
            etAddress.requestFocus()
            return
        }

        // Register user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val userId = firebaseUser?.uid ?: return@addOnCompleteListener
                    val formattedName = name.replace(" ", "_").lowercase() // Replace spaces with underscores

                    val user = User(
                        id = userId,
                        name = name,
                        email = email,
                        password = password,
                        mobile = mobile,
                        address = address,
                        imageName = formattedName // Store formatted name as image name
                    )

                    // Save user details in Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                                clearFormFields()

                                // Navigate to LoginFragment
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .replace(R.id.frame_container, LoginFragment())
                                    .addToBackStack(null)
                                    .commit()
                            } else {
                                Toast.makeText(requireContext(), "Failed to save user data!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearFormFields() {
        etName.text.clear()
        etEmail.text.clear()
        etPassword.text.clear()
        etConfirmPassword.text.clear()
        etMobile.text.clear()
        etAddress.text.clear()
    }
}
