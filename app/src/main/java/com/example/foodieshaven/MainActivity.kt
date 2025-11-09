package com.example.foodieshaven

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private fun applyClickEffect(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val profileIcon: ImageView = findViewById(R.id.icon_profile)
        val cartIcon: ImageView = findViewById(R.id.icon_cart)
        //val wishlistIcon: ImageView = findViewById(R.id.icon_wishlist)

        profileIcon.setOnClickListener {
            // Show the profile dialog when the profile icon is clicked
            val profileDialog = ProfileFragment()
            profileDialog.show(supportFragmentManager, "ProfileDialog")
        }

        cartIcon.setOnClickListener {
            applyClickEffect(it)
            replaceFragment(CartFragment())
        }

        /*wishlistIcon.setOnClickListener {
            applyClickEffect(it)
            replaceFragment(WishlistFragment())
        }*/

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bottom_category -> {
                    replaceFragment(CategoryFragment())
                    true
                }
                R.id.bottom_search -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.bottom_contact -> {
                    replaceFragment(ContactFragment())
                    true
                }
                R.id.bottom_order -> {
                    replaceFragment(OrderFragment())
                    true
                }
                else -> false
            }
        }

        // Load HomeFragment only if there is no saved state
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
