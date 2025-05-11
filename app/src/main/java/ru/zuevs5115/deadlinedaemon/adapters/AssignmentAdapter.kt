package ru.zuevs5115.deadlinedaemon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ru.zuevs5115.deadlinedaemon.databinding.ActivityAssignmentsBinding
import ru.zuevs5115.deadlinedaemon.databinding.ItemAssignmentBinding
import ru.zuevs5115.deadlinedaemon.enities.Assignment

class AssignmentAdapter(private val assignments: List<Assignment>) :
    RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    inner class AssignmentViewHolder(val binding: ItemAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignments[position]
        holder.binding.tvTitle.text = assignment.title
        holder.binding.tvDeadline.text = "Дедлайн: ${assignment.deadline}"
        holder.binding.tvGroups.text = "Группы: ${assignment.groups.joinToString()}"
        holder.binding.tvSubject.text = "Предмет: ${assignment.subject}"
    }

    override fun getItemCount(): Int = assignments.size
}
