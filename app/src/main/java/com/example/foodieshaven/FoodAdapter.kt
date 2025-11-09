package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FoodAdapter(
    private val foods: List<Food>,
    private val onOrderClick: (Food) -> Unit,
    private val onAddToCartClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return FoodViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foods[position]
        holder.bind(food)
    }

    override fun getItemCount() = foods.size

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImageView: ImageView = itemView.findViewById(R.id.foodImageView)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)
        private val foodPriceTextView: TextView = itemView.findViewById(R.id.foodPriceTextView)
        private val orderButton: Button = itemView.findViewById(R.id.orderButton)
        private val cartButton: ImageView = itemView.findViewById(R.id.cartButton)

        fun bind(food: Food) {
            foodNameTextView.text = food.title
            foodPriceTextView.text = String.format("Rs.%.2f", food.price)

            // Load food image
            if (food.imageName.startsWith("http")) {
                Glide.with(foodImageView.context).load(food.imageName).into(foodImageView)
            } else {
                val context = foodImageView.context
                val imageResource =
                    context.resources.getIdentifier(food.imageName, "drawable", context.packageName)
                if (imageResource != 0) {
                    foodImageView.setImageResource(imageResource)
                } else {
                    foodImageView.setImageResource(R.drawable.ice_cream)
                }
            }

            orderButton.setOnClickListener {
                onOrderClick(food) // Ensures login check before ordering
            }

            cartButton.setOnClickListener {
                onAddToCartClick(food) // Ensures login check before adding to cart
            }
        }
    }
}
