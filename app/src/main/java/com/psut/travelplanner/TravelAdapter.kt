package com.psut.travelplanner

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TravelAdapter(private val context: Context) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    private var travelList: MutableList<TravelItinerary> = mutableListOf()
    private var onItemClickListener: OnItemClickListener? = null

    // Define the click listener interface
    interface OnItemClickListener {
        fun onItemClick(travelId: Int)
    }

    // Set the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel, parent, false)
        return TravelViewHolder(itemView)
    }

    override fun getItemCount() = travelList.size

    // ViewHolder class
    inner class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val timeTextView: TextView = itemView.findViewById(R.id.time)

        init {
            // Set click listener for item
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                // Get the clicked travel item
                if (position != RecyclerView.NO_POSITION) {
                    val clickedTravel = travelList[position]
                    onItemClickListener?.onItemClick(clickedTravel.id)

                    // Handle item click to navigate to Add/Edit Activity or view travel details
                    val intent = Intent(context, AddEditActivity::class.java)
                    intent.putExtra("TRAVEL_ID", clickedTravel.id) // Pass the travel ID if needed
                    context.startActivity(intent)
                }
            }
        }
    }

    fun setTravelList(travels: List<TravelItinerary>) {
        travelList.clear()
        travelList.addAll(travels)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travel = travelList[position]

        // Bind the travel data to the views in the ViewHolder
        holder.titleTextView.text = travel.title
        holder.dateTextView.text = travel.date
        holder.timeTextView.text = travel.time
        // Bind other views as needed
    }

}
