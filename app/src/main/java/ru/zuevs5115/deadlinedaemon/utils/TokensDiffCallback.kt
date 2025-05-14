package ru.zuevs5115.deadlinedaemon.utils

import androidx.recyclerview.widget.DiffUtil
import ru.zuevs5115.deadlinedaemon.entities.AdminToken
import ru.zuevs5115.deadlinedaemon.entities.Subject

//optimal update content in RecyclerView
class TokensDiffCallback(
    //old elements
    private val oldList: List<AdminToken>,
    //new elements
    private val newList: List<AdminToken>
) : DiffUtil.Callback() {

    //old size
    override fun getOldListSize(): Int = oldList.size
    //new size
    override fun getNewListSize(): Int = newList.size

    //check if this the same item (removes completely other elements)
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    //check if the same items have same content
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}