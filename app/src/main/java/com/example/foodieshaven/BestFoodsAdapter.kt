package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BestFoodsAdapter(
    private val foodList: List<Food>,
    private val onOrderClick: (Food) -> Unit,
    private val onAddToCartClick: (Food) -> Unit
) : RecyclerView.Adapter<BestFoodsAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val orderButton: Button = view.findViewById(R.id.orderButton)
        val cartButton: ImageView = view.findViewById(R.id.cartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_best_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.foodName.text = food.title
        holder.foodPrice.text = "Rs.${food.price}"

        val context = holder.itemView.context
        if (food.imageName.startsWith("http")) {
            // Load from URL
            Glide.with(context).load(food.imageName).into(holder.foodImage)
        } else {
            // Load from drawable
            val imageResource = context.resources.getIdentifier(food.imageName, "drawable", context.packageName)
            if (imageResource != 0) {
                holder.foodImage.setImageResource(imageResource)
            } else {
                holder.foodImage.setImageResource(R.drawable.ice_cream) // Default image
            }
        }

        // Handle button clicks
        holder.orderButton.setOnClickListener {
            onOrderClick(food)
        }

        holder.cartButton.setOnClickListener {
            onAddToCartClick(food)
        }
    }

    override fun getItemCount(): Int = foodList.size
}
