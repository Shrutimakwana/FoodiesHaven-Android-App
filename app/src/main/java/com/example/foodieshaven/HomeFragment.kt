package com.example.foodieshaven

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.widget.Toast
import android.widget.TextView
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var homeCategoryAdapter: HomeCategoryAdapter
    private lateinit var bestFoodsAdapter: BestFoodsAdapter
    private val categoryList = mutableListOf<Category>()
    private val bestFoodsList = mutableListOf<Food>()

    private lateinit var database: DatabaseReference
    private lateinit var cartDatabase: DatabaseReference
    private lateinit var foodDatabase: DatabaseReference
    private lateinit var sliderContainer: LinearLayout
    private lateinit var sliderScrollView: HorizontalScrollView
    private lateinit var bestFoodsRecyclerView: RecyclerView

    private val imageResources = listOf(R.drawable.home1, R.drawable.home2, R.drawable.home3)
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Slider References
        sliderScrollView = view.findViewById(R.id.sliderScrollView)
        sliderContainer = view.findViewById(R.id.sliderContainer)
        bestFoodsRecyclerView = view.findViewById(R.id.bestFoodsRecyclerView)

        // Add images to the slider
        for (imageRes in imageResources) {
            val cardView = createCardView(imageRes)
            sliderContainer.addView(cardView)
        }
        startAutoSlider()

        // RecyclerView for Categories
        val categoryRecyclerView: RecyclerView = view.findViewById(R.id.homeCategoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // RecyclerView for Best Foods
        bestFoodsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Firebase Database References
        database = FirebaseDatabase.getInstance().reference.child("categories")
        foodDatabase = FirebaseDatabase.getInstance().reference.child("foods")
        cartDatabase = FirebaseDatabase.getInstance().reference.child("cart")

        // Fetch data from Firebase
        fetchCategoriesFromFirebase()
        fetchBestFoodsFromFirebase()

        // Initialize adapters
        homeCategoryAdapter = HomeCategoryAdapter(categoryList) { category -> navigateToFoodFragment(category) }
        categoryRecyclerView.adapter = homeCategoryAdapter

        bestFoodsAdapter = BestFoodsAdapter(bestFoodsList,
            onOrderClick = { selectedFood -> checkAndOrderNow(selectedFood) },
            onAddToCartClick = { selectedFood -> checkAndAddToCart(selectedFood) }
        )

        bestFoodsRecyclerView.adapter = bestFoodsAdapter

        val aboutText: TextView = view.findViewById(R.id.aboutText)

        // Fade in animation for the About Us section
        aboutText.alpha = 0f
        aboutText.animate().alpha(1f).setDuration(1000).start()

        view.findViewById<Button>(R.id.exploreMenuButton).setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, CategoryFragment()) // Replace the existing fragment
                .addToBackStack(null) // Keep back navigation enabled
                .commit()
        }

        val whatsappIcon: ImageView = view.findViewById(R.id.whatsappIcon)
        val instagramIcon: ImageView = view.findViewById(R.id.instagramIcon)
        val gmailIcon: ImageView = view.findViewById(R.id.gmailIcon)

        whatsappIcon.setOnClickListener {
            try {
                val phoneNumber = "+91 9974401442" // Replace with your WhatsApp number
                val url = "https://wa.me/$phoneNumber"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }

        instagramIcon.setOnClickListener {
            val username = "shrutimakwana113" // Replace with your Instagram username
            val uri = Uri.parse("http://instagram.com/_u/$username") // Deep link
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.instagram.android")

            try {
                startActivity(intent) // Open Instagram app
            } catch (e: Exception) {
                // Open Instagram in the browser if the app is not installed
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/$username/"))
                startActivity(webIntent)
            }
        }

        gmailIcon.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:shrutimakwana113@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Hello from FoodiesHaven") // Pre-fill subject
                    putExtra(Intent.EXTRA_TEXT, "Hi, I am interested in your services!") // Pre-fill message
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Function to create a CardView with an image
    private fun createCardView(imageRes: Int): CardView {
        val screenWidth = resources.displayMetrics.widthPixels
        val imageWidth = (screenWidth * 0.9).toInt()
        val imageHeight = (imageWidth * 0.6).toInt()

        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                imageWidth,
                imageHeight
            ).apply {
                marginEnd = 10.dpToPx()
            }
            radius = 14f
            cardElevation = 5f
        }

        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            adjustViewBounds = true
            setImageResource(imageRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        cardView.addView(imageView)
        return cardView
    }

    // Fetch categories from Firebase
    private fun fetchCategoriesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No categories available", Toast.LENGTH_SHORT).show()
                    return
                }
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let {
                        categoryList.add(it)
                    }
                }
                homeCategoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Database Error: ${error.message}")
                Toast.makeText(context, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch Best Foods from Firebase
    private fun fetchBestFoodsFromFirebase() {
        foodDatabase.orderByChild("bestFood").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bestFoodsList.clear()
                for (foodSnapshot in snapshot.children) {
                    val food = foodSnapshot.getValue(Food::class.java)
                    food?.let { bestFoodsList.add(it) }
                }
                bestFoodsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Database Error: ${error.message}")
            }
        })
    }

    private fun navigateToFoodFragment(category: Category) {
        Log.d("HomeFragment", "Navigating to FoodFragment with category: ${category.title}")

        val foodFragment = FoodFragment().apply {
            arguments = Bundle().apply {
                putString("categoryId", category.id)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.frame_container, foodFragment, FoodFragment::class.java.simpleName)
            .addToBackStack(null)
            .commit()

        Log.d("HomeFragment", "FoodFragment committed successfully.")
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    // Auto Slider Handler
    private val sliderRunnable = object : Runnable {
        override fun run() {
            if (sliderContainer.childCount > 0) {
                currentIndex = (currentIndex + 1) % sliderContainer.childCount
                sliderScrollView.smoothScrollTo(
                    sliderContainer.getChildAt(currentIndex).left, 0
                )
                handler.postDelayed(this, 3000)
            }
        }
    }

    private fun startAutoSlider() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (sliderContainer.childCount > 0) {
                    currentIndex = (currentIndex + 1) % sliderContainer.childCount
                    sliderScrollView.smoothScrollTo(
                        sliderContainer.getChildAt(currentIndex).left, 0
                    )

                    // Fade-in animation for active image
                    val activeCard = sliderContainer.getChildAt(currentIndex)
                    activeCard.animate().alpha(1f).setDuration(500).start()

                    handler.postDelayed(this, 3000)
                }
            }
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(sliderRunnable)
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
