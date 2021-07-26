package com.router.battery.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.router.battery.R
import com.router.battery.models.InfoCard

class ItemAdapter(
    private val context: Context,
    private val valuesList: List<InfoCard>)
    : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view){
        val title: TextView = view.findViewById(R.id.item_title)
        val description: TextView = view.findViewById(R.id.item_description)
        val icon: ImageView = view.findViewById(R.id.item_image)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(adapterLayout)


    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = valuesList[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.icon.setImageResource(item.imageResourceId)


    }

    override fun getItemCount() = valuesList.size


}