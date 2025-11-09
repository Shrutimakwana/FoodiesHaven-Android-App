package com.example.foodieshaven

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val cartItems: MutableList<Cart>,
    private val onQuantityChanged: (Cart) -> Unit,
    private val onRemoveClicked: (Cart) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cartImageView: ImageView = itemView.findViewById(R.id.cartImageView)
        private val cartNameTextView: TextView = itemView.findViewById(R.id.cartNameTextView)
        private val cartPriceTextView: TextView = itemView.findViewById(R.id.cartPriceTextView)
        private val decreaseQuantityButton: Button = itemView.findViewById(R.id.decreaseQuantityButton)
        private val increaseQuantityButton: Button = itemView.findViewById(R.id.increaseQuantityButton)
        private val cartQuantityTextView: TextView = itemView.findViewById(R.id.cartQuantityTextView)
        private val removeButton: Button = itemView.findViewById(R.id.removeButton)

        fun bind(cart: Cart) {
            cartNameTextView.text = cart.title
            cartPriceTextView.text = String.format("Rs.%.2f", cart.price)
            cartQuantityTextView.text = cart.quantity.toString()

            // Load image
            if (cart.imageName.startsWith("http")) {
                Glide.with(cartImageView.context).load(cart.imageName).into(cartImageView)
            } else {
                val context = cartImageView.context
                val imageResource =
                    context.resources.getIdentifier(cart.imageName, "drawable", context.packageName)
                if (imageResource != 0) {
                    cartImageView.setImageResource(imageResource)
                } else {
                    cartImageView.setImageResource(R.drawable.ice_cream)
                }
            }

            decreaseQuantityButton.setOnClickListener {
                if (cart.quantity > 1) {
                    cart.quantity--
                    cartQuantityTextView.text = cart.quantity.toString()
                    onQuantityChanged(cart)
                }
            }

            increaseQuantityButton.setOnClickListener {
                cart.quantity++
                cartQuantityTextView.text = cart.quantity.toString()
                onQuantityChanged(cart)
            }

            removeButton.setOnClickListener {
                onRemoveClicked(cart)
            }
        }
    }
}
