package ru.zuevs5115.deadlinedaemon.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ItemAssignmentBinding
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.utils.AssignmentDiffCallback

//adapter for display set of assignments
class AssignmentAdapter(private var assignments: List<Assignment>) :
    RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {
    //stores links to markup elements of a single list item
    inner class AssignmentViewHolder(val binding: ItemAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root)
    //create container for item store
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding)
    }
    //fill data in new ViewHolder
    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        //fill data
        val assignment = assignments[position]
        val context = holder.binding.root.context
        holder.binding.tvTitle.text = assignment.title
        holder.binding.tvDeadline.text = context.getString(R.string.deadline_field_ph, assignment.deadline)
        holder.binding.tvGroups.text = context.getString(R.string.groups_field_ph, assignment.groups.joinToString())
        holder.binding.tvSubject.text = context.getString(R.string.subject_field_ph, assignment.subject)
        //Add process long click
        holder.itemView.setOnLongClickListener {
            onItemLongClick(assignment)
            true
        }
    }
    //update data information
    fun updateData(newAssignments: List<Assignment>) {
        //use DiffUtil for optimal update of content
        val diffCallback = AssignmentDiffCallback(assignments, newAssignments)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        //set assignments
        assignments = newAssignments
        //update UI (optimal)
        diffResult.dispatchUpdatesTo(this)
    }
    //process item with long click
    private fun onItemLongClick(assignment: Assignment) {

    }
    //count of items
    override fun getItemCount(): Int = assignments.size
}
