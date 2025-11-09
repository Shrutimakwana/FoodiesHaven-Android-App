package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeCategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<HomeCategoryAdapter.HomeCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_category_item, parent, false)
        return HomeCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HomeCategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount() = categories.size

    inner class HomeCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView)
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryCard: CardView = itemView.findViewById(R.id.categoryCard)

        init {
            categoryCard.setOnClickListener {
                onCategoryClick(categories[adapterPosition])
            }
        }

        fun bind(category: Category) {
            categoryNameTextView.text = category.title

            val context = categoryImageView.context
            val imageResource = context.resources.getIdentifier(category.imageUrl, "drawable", context.packageName)

            if (imageResource != 0) {
                Glide.with(context)
                    .load(imageResource)
                    .into(categoryImageView)
            } else {
                categoryImageView.setImageResource(R.drawable.ic_launcher_foreground) // Fallback image
            }
        }
    }
}
