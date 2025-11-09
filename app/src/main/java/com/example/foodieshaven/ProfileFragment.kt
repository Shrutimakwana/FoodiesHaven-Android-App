package com.example.foodieshaven

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ProfileFragment : DialogFragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var btnLogin: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // Initialize Views
        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Fetch user data
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserDetails(currentUser.uid) // Fetch data from Firebase Database
        } else {
            showLoginButton()
        }

        // Login button click event
        btnLogin.setOnClickListener {
            navigateToLogin()
        }

        // Logout button click event
        btnLogout.setOnClickListener {
            logoutUser()
            dismiss()
        }

        return view
    }

    private fun fetchUserDetails(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    if (user != null) {
                        tvName.text = user.name
                        tvEmail.text = user.email

                        // Load image from drawable if imageName exists
                        val resourceId = resources.getIdentifier(user.imageName, "drawable", requireContext().packageName)

                        if (resourceId != 0) {
                            Glide.with(requireContext())
                                .load(resourceId)
                                .circleCrop() // Ensures circular shape
                                .into(ivProfilePic)
                        } else {
                            Glide.with(requireContext())
                                .load(R.drawable.baseline_profile_24)
                                .circleCrop()
                                .into(ivProfilePic)
                        }
                        btnLogin.visibility = View.GONE
                        btnLogout.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoginButton() {
        btnLogin.visibility = View.VISIBLE
        btnLogout.visibility = View.GONE
    }

    private fun navigateToLogin() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, LoginFragment())
            .addToBackStack(null)
            .commit()

        dismiss()
    }

    private fun logoutUser() {
        auth.signOut()
        showLoginButton()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
