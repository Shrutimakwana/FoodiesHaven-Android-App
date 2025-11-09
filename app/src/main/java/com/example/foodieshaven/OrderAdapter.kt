package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OrderAdapter(
    private val orderList: List<Order>,
    private val onCancelClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImageView)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val cancelButton: Button = itemView.findViewById(R.id.cancelOrderButton)
        val customerName: TextView = itemView.findViewById(R.id.customerName)
        val customerPhone: TextView = itemView.findViewById(R.id.customerPhone)
        val customerAddress: TextView = itemView.findViewById(R.id.customerAddress)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.foodName.text = "Food: ${order.foodName}"
        holder.totalPrice.text = "Total: Rs. ${order.totalPrice}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.customerName.text = "Name: ${order.customerName}"
        holder.customerPhone.text = "Phone: ${order.customerPhone}"
        holder.customerAddress.text = "Address: ${order.customerAddress}"

        // Load image from drawable using imageName from the Order model
        val context = holder.foodImage.context
        val imageResource = context.resources.getIdentifier(order.imageName, "drawable", context.packageName)

        if (imageResource != 0) {
            Glide.with(context)
                .load(imageResource)
                .into(holder.foodImage)
        } else {
            holder.foodImage.setImageResource(R.drawable.ic_launcher_foreground) // Default fallback
        }

        // Control the visibility of the cancel button based on order status
        if (order.status == "Delivered") {
            holder.cancelButton.visibility = View.GONE // Hide cancel button if order is delivered
        } else {
            holder.cancelButton.visibility = View.VISIBLE // Show cancel button for other statuses
        }

        // Set the cancel button click listener
        holder.cancelButton.setOnClickListener {
            onCancelClick(order) // Trigger cancel action
        }
    }

    override fun getItemCount(): Int = orderList.size
}
