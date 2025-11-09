package com.example.foodieshaven

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var auth: FirebaseAuth  // Firebase Authentication instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        // Initialize Views
        etEmail = view.findViewById(R.id.etEmail)
        btnResetPassword = view.findViewById(R.id.btnResetPassword)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Set Click Listener for Reset Password Button
        btnResetPassword.setOnClickListener {
            resetPassword()
        }

        return view
    }

    private fun resetPassword() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()

                    // Navigate back to the Login screen after success
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, LoginFragment()) // Ensure correct container ID
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
