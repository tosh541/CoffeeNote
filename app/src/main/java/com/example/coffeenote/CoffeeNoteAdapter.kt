package com.example.coffeenote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class CoffeeNoteAdapter(data: OrderedRealmCollection<CoffeeNote>) :
        RealmRecyclerViewAdapter<CoffeeNote, CoffeeNoteAdapter.ViewHolder>(data, true) {

    private var listener: ((Long?) -> Unit)? = null

    fun setOnItemClickListener(listener:(Long?) -> Unit) {
        this.listener = listener
    }

    init {
        setHasStableIds(true)
    }

    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
        val title: TextView = cell.findViewById(android.R.id.text1)
        val total: TextView = cell.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CoffeeNoteAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoffeeNoteAdapter.ViewHolder, position: Int) {
        val coffeeNote: CoffeeNote? = getItem(position)
        holder.title.text = coffeeNote?.title
        holder.total.text = ("星" + coffeeNote?.total.toString())
        holder.itemView.setOnClickListener {
            listener?.invoke(coffeeNote?.id)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}