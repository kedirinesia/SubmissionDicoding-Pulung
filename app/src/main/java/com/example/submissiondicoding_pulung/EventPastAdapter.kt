package com.example.submissiondicoding_pulung

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventPastAdapter(private val eventList: List<EventPast>) :
    RecyclerView.Adapter<EventPastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.eventName1)
        val eventImage: ImageView = view.findViewById(R.id.eventImage1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_past_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventName.text = event.name


        Glide.with(holder.itemView.context)
            .load(event.imageLogo)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image)
            .into(holder.eventImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EventDetailActivity::class.java).apply {
                putExtra("eventId", event.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = eventList.size
}
