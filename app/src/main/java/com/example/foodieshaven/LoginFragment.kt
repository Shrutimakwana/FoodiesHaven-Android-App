package com.example.foodieshaven

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView

    private lateinit var auth: FirebaseAuth  // Firebase Authentication instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Views
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvForgotPassword = view.findViewById(R.id.forgotPassword)
        tvRegister = view.findViewById(R.id.tvRegister)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Login Button Click Listener
        btnLogin.setOnClickListener {
            performLogin()
        }

        // Forgot Password Click Listener
        tvForgotPassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, ForgotPasswordFragment()) // Ensure correct container ID
                .addToBackStack(null)
                .commit()
        }

        // Navigate to RegisterFragment when "Register" is clicked
        tvRegister.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.frame_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email cannot be empty"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password cannot be empty"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, HomeFragment()) // Ensure correct container ID
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
