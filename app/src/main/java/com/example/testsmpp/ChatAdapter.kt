package com.example.testsmpp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatItems: List<ChatItem>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    // ViewHolder class holds references to the views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatItem = chatItems[position]
        holder.phoneNumberTextView.text = chatItem.phoneNumber
        holder.messageTextView.text = chatItem.message
    }

    // Return the item count
    override fun getItemCount(): Int {
        return chatItems.size
    }


    data class ChatItem(
        val phoneNumber: String,
        val message: String,
    )
}