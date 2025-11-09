package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CategoryAdapter(private val categories: List<Category>, private val onCategoryClick: (Category) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount() = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImageView)
        private val categoryNameTextView: TextView =
            itemView.findViewById(R.id.categoryNameTextView)

        init {
            itemView.setOnClickListener {
                val category = categories[adapterPosition]
                onCategoryClick(category)
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
