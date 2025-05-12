package ru.zuevs5115.deadlinedaemon.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ItemSubjectBinding
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.AssignmentDiffCallback
import ru.zuevs5115.deadlinedaemon.utils.SubjectDiffCallback

//adapter for display set of assignments
class SubjectAdapter(private var subjects: List<Subject>,
                     private val onItemLongClick: (Subject) -> Unit) :
    RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {
    //stores links to markup elements of a single list item
    inner class SubjectViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)
    //create container for item store
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }
    //fill data in new ViewHolder
    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        //fill data
        val subject = subjects[position]
        holder.binding.tvName.text = subject.name
        //Add process long click
        holder.itemView.setOnLongClickListener {
            onItemLongClick(subject)
            true
        }
    }
    //update data information
    fun updateData(newSubjects: List<Subject>) {
        //use DiffUtil for optimal update of content
        val diffCallback = SubjectDiffCallback(subjects, newSubjects)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        //set assignments
        subjects = newSubjects
        //update UI (optimal)
        diffResult.dispatchUpdatesTo(this)
    }
    //count of items
    override fun getItemCount(): Int = subjects.size
}
