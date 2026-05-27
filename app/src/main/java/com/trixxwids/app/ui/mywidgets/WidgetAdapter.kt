package com.trixxwids.app.ui.mywidgets

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.databinding.ItemWidgetBinding
import com.trixxwids.app.util.WidgetSizeUtil

class WidgetAdapter(
    private val onEdit: (WidgetEntity) -> Unit,
    private val onDuplicate: (WidgetEntity) -> Unit,
    private val onDelete: (WidgetEntity) -> Unit,
    private val onApplyToHome: (WidgetEntity) -> Unit
) : ListAdapter<WidgetEntity, WidgetAdapter.WidgetViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val binding = ItemWidgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WidgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WidgetViewHolder(
        private val binding: ItemWidgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(widget: WidgetEntity) {
            binding.widgetName.text = widget.name
            binding.widgetSize.text = WidgetSizeUtil.getSizeLabel(widget.sizeTag)

            if (widget.thumbnailPath != null) {
                try {
                    val bm = BitmapFactory.decodeFile(widget.thumbnailPath)
                    if (bm != null) {
                        binding.widgetThumbnail.setImageBitmap(bm)
                    }
                } catch (_: Exception) {}
            }

            binding.cardWidget.setOnClickListener {
                onEdit(widget)
            }

            binding.cardWidget.setOnLongClickListener {
                showContextMenu(widget)
                true
            }
        }

        private fun showContextMenu(widget: WidgetEntity) {
            val items = arrayOf("Edit", "Duplicate", "Delete", "Apply to Home Screen")
            androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle(widget.name)
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> onEdit(widget)
                        1 -> onDuplicate(widget)
                        2 -> onDelete(widget)
                        3 -> onApplyToHome(widget)
                    }
                }
                .show()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WidgetEntity>() {
        override fun areItemsTheSame(oldItem: WidgetEntity, newItem: WidgetEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WidgetEntity, newItem: WidgetEntity): Boolean {
            return oldItem == newItem
        }
    }
}
