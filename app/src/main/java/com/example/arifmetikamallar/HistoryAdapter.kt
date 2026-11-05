package com.example.arifmetikamallar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arifmetikamallar.data.HistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var items: MutableList<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit,
    private val onCopyClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem, Int) -> Unit,
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val tvExpression: TextView = itemView.findViewById(R.id.tvHistoryExpression)
        val tvResult: TextView = itemView.findViewById(R.id.tvHistoryResult)
        val btnCopy: ImageButton = itemView.findViewById(R.id.btnCopyItem)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = dateFormat.format(Date(item.timestamp))
        holder.tvExpression.text = item.expression
        holder.tvResult.text = item.result

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.btnCopy.setOnClickListener {
            onCopyClick(item)
        }

        holder.btnDelete.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onDeleteClick(item, currentPos)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newItems: List<HistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
