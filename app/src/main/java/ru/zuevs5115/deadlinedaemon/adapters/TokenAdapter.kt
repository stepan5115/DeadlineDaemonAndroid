package ru.zuevs5115.deadlinedaemon.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.zuevs5115.deadlinedaemon.R
import ru.zuevs5115.deadlinedaemon.databinding.ItemSubjectBinding
import ru.zuevs5115.deadlinedaemon.entities.AdminToken
import ru.zuevs5115.deadlinedaemon.entities.Assignment
import ru.zuevs5115.deadlinedaemon.entities.Subject
import ru.zuevs5115.deadlinedaemon.utils.AssignmentDiffCallback
import ru.zuevs5115.deadlinedaemon.utils.SubjectDiffCallback
import ru.zuevs5115.deadlinedaemon.utils.TokensDiffCallback

//adapter for display set of assignments
class TokenAdapter(
    private var tokens: List<AdminToken>,
    private val onItemClick: (AdminToken) -> Unit
) : RecyclerView.Adapter<TokenAdapter.TokenViewHolder>() {

    private var onItemLongClick: ((AdminToken) -> Boolean)? = null

    fun setOnItemLongClickListener(listener: (AdminToken) -> Boolean) {
        onItemLongClick = listener
    }

    inner class TokenViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TokenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokens[position]
        holder.binding.tvName.text = token.token

        holder.itemView.setOnClickListener {
            onItemClick(token)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(token) ?: false
        }
    }

    override fun getItemCount(): Int = tokens.size

    fun updateData(newTokens: List<AdminToken>) {
        val diffCallback = TokensDiffCallback(tokens, newTokens)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tokens = newTokens
        diffResult.dispatchUpdatesTo(this)
    }
}