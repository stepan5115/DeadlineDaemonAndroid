package ru.zuevs5115.deadlinedaemon.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ItemGroupBinding
import ru.zuevs5115.deadlinedaemon.databinding.ItemSubjectBinding
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.Group
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.AssignmentDiffCallback
import ru.zuevs5115.deadlinedaemon.utils.GroupDiffCallback
import ru.zuevs5115.deadlinedaemon.utils.SubjectDiffCallback

//adapter for display set of assignments
class GroupAdapter(private var groups: List<Group>,
                   private val onItemLongClick: (Group) -> Unit) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    //stores links to markup elements of a single list item
    inner class GroupViewHolder(val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root)
    //create container for item store
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }
    //fill data in new ViewHolder
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        //fill data
        val group = groups[position]
        holder.binding.tvName.text = group.name
        //Add process long click
        holder.itemView.setOnLongClickListener {
            onItemLongClick(group)
            true
        }
    }
    //update data information
    fun updateData(newGroups: List<Group>) {
        //use DiffUtil for optimal update of content
        val diffCallback = GroupDiffCallback(groups, newGroups)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        //set assignments
        groups = newGroups
        //update UI (optimal)
        diffResult.dispatchUpdatesTo(this)
    }
    //count of items
    override fun getItemCount(): Int = groups.size
}