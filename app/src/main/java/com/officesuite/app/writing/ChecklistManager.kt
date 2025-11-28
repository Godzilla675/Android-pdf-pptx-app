package com.officesuite.app.writing

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.util.*

/**
 * Interactive Checklist Manager for Note-Taking
 * Part of Medium Priority Features Phase 2: Advanced Note-Taking
 * 
 * Features:
 * - Create and manage interactive checklists
 * - Drag and drop reordering
 * - Strike-through completed items
 * - Progress tracking
 * - Export to markdown/text
 */
class ChecklistManager {

    data class ChecklistItem(
        val id: String = UUID.randomUUID().toString(),
        var text: String,
        var isChecked: Boolean = false,
        var priority: Priority = Priority.NORMAL,
        var dueDate: Long? = null,
        val createdAt: Long = System.currentTimeMillis()
    )

    enum class Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    data class Checklist(
        val id: String = UUID.randomUUID().toString(),
        var title: String,
        val items: MutableList<ChecklistItem> = mutableListOf(),
        val createdAt: Long = System.currentTimeMillis(),
        var lastModified: Long = System.currentTimeMillis()
    )

    /**
     * Create a new checklist
     */
    fun createChecklist(title: String): Checklist {
        return Checklist(title = title)
    }

    /**
     * Add item to checklist
     */
    fun addItem(checklist: Checklist, text: String, priority: Priority = Priority.NORMAL): ChecklistItem {
        val item = ChecklistItem(text = text, priority = priority)
        checklist.items.add(item)
        checklist.lastModified = System.currentTimeMillis()
        return item
    }

    /**
     * Remove item from checklist
     */
    fun removeItem(checklist: Checklist, itemId: String) {
        checklist.items.removeAll { it.id == itemId }
        checklist.lastModified = System.currentTimeMillis()
    }

    /**
     * Toggle item checked state
     */
    fun toggleItem(checklist: Checklist, itemId: String): Boolean {
        val item = checklist.items.find { it.id == itemId }
        return if (item != null) {
            item.isChecked = !item.isChecked
            checklist.lastModified = System.currentTimeMillis()
            item.isChecked
        } else {
            false
        }
    }

    /**
     * Reorder items
     */
    fun moveItem(checklist: Checklist, fromIndex: Int, toIndex: Int) {
        if (fromIndex in checklist.items.indices && toIndex in checklist.items.indices) {
            val item = checklist.items.removeAt(fromIndex)
            checklist.items.add(toIndex, item)
            checklist.lastModified = System.currentTimeMillis()
        }
    }

    /**
     * Get checklist progress
     */
    fun getProgress(checklist: Checklist): Float {
        if (checklist.items.isEmpty()) return 0f
        val completedCount = checklist.items.count { it.isChecked }
        return completedCount.toFloat() / checklist.items.size
    }

    /**
     * Get completed items count
     */
    fun getCompletedCount(checklist: Checklist): Int {
        return checklist.items.count { it.isChecked }
    }

    /**
     * Get pending items count
     */
    fun getPendingCount(checklist: Checklist): Int {
        return checklist.items.count { !it.isChecked }
    }

    /**
     * Sort items by various criteria
     */
    fun sortItems(checklist: Checklist, sortBy: SortBy) {
        when (sortBy) {
            SortBy.PRIORITY_DESC -> checklist.items.sortByDescending { it.priority.ordinal }
            SortBy.PRIORITY_ASC -> checklist.items.sortBy { it.priority.ordinal }
            SortBy.CREATED_DESC -> checklist.items.sortByDescending { it.createdAt }
            SortBy.CREATED_ASC -> checklist.items.sortBy { it.createdAt }
            SortBy.CHECKED_FIRST -> checklist.items.sortByDescending { it.isChecked }
            SortBy.UNCHECKED_FIRST -> checklist.items.sortBy { it.isChecked }
            SortBy.ALPHABETICAL -> checklist.items.sortBy { it.text.lowercase() }
        }
        checklist.lastModified = System.currentTimeMillis()
    }

    enum class SortBy {
        PRIORITY_DESC, PRIORITY_ASC, CREATED_DESC, CREATED_ASC,
        CHECKED_FIRST, UNCHECKED_FIRST, ALPHABETICAL
    }

    /**
     * Export checklist to markdown format
     */
    fun exportToMarkdown(checklist: Checklist): String {
        val builder = StringBuilder()
        builder.appendLine("# ${checklist.title}")
        builder.appendLine()
        
        checklist.items.forEach { item ->
            val checkbox = if (item.isChecked) "[x]" else "[ ]"
            val priorityTag = when (item.priority) {
                Priority.URGENT -> " 🔴"
                Priority.HIGH -> " 🟠"
                Priority.NORMAL -> ""
                Priority.LOW -> " 🟢"
            }
            builder.appendLine("- $checkbox ${item.text}$priorityTag")
        }
        
        val progress = (getProgress(checklist) * 100).toInt()
        builder.appendLine()
        builder.appendLine("---")
        builder.appendLine("Progress: $progress% (${getCompletedCount(checklist)}/${checklist.items.size})")
        
        return builder.toString()
    }

    /**
     * Export checklist to plain text format
     */
    fun exportToText(checklist: Checklist): String {
        val builder = StringBuilder()
        builder.appendLine(checklist.title)
        builder.appendLine("=".repeat(checklist.title.length))
        builder.appendLine()
        
        checklist.items.forEach { item ->
            val checkbox = if (item.isChecked) "[✓]" else "[ ]"
            builder.appendLine("$checkbox ${item.text}")
        }
        
        val progress = (getProgress(checklist) * 100).toInt()
        builder.appendLine()
        builder.appendLine("Progress: $progress%")
        
        return builder.toString()
    }

    /**
     * Parse markdown checklist format
     */
    fun parseFromMarkdown(markdown: String): Checklist? {
        val lines = markdown.lines()
        
        // Find title (first heading)
        val titleLine = lines.find { it.startsWith("# ") } ?: return null
        val title = titleLine.removePrefix("# ").trim()
        
        val checklist = createChecklist(title)
        
        // Parse items
        val checkboxPattern = Regex("^-\\s*\\[([ xX])\\]\\s*(.+)$")
        lines.forEach { line ->
            val match = checkboxPattern.find(line.trim())
            if (match != null) {
                val isChecked = match.groupValues[1].lowercase() == "x"
                val text = match.groupValues[2].trim()
                    .replace(" 🔴", "")
                    .replace(" 🟠", "")
                    .replace(" 🟢", "")
                    .trim()
                
                val priority = when {
                    line.contains("🔴") -> Priority.URGENT
                    line.contains("🟠") -> Priority.HIGH
                    line.contains("🟢") -> Priority.LOW
                    else -> Priority.NORMAL
                }
                
                val item = addItem(checklist, text, priority)
                item.isChecked = isChecked
            }
        }
        
        return if (checklist.items.isNotEmpty()) checklist else null
    }
}

/**
 * RecyclerView Adapter for Checklist items
 */
class ChecklistAdapter(
    private val checklistManager: ChecklistManager,
    private val checklist: ChecklistManager.Checklist,
    private val onItemChanged: () -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewWithTag("checkbox")
        val textView: TextView = view.findViewWithTag("text")
        val deleteButton: ImageButton = view.findViewWithTag("delete")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)
        }

        val checkBox = CheckBox(parent.context).apply {
            tag = "checkbox"
        }
        layout.addView(checkBox)

        val textView = TextView(parent.context).apply {
            tag = "text"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(16, 0, 16, 0)
            textSize = 16f
        }
        layout.addView(textView)

        val deleteButton = ImageButton(parent.context).apply {
            tag = "delete"
            setBackgroundColor(0)
            contentDescription = "Delete"
        }
        layout.addView(deleteButton)

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = checklist.items[position]
        
        holder.checkBox.isChecked = item.isChecked
        holder.textView.text = item.text
        
        // Apply strikethrough for completed items
        if (item.isChecked) {
            holder.textView.paintFlags = holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.textView.alpha = 0.6f
        } else {
            holder.textView.paintFlags = holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.textView.alpha = 1.0f
        }
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            notifyItemChanged(position)
            onItemChanged()
        }
        
        holder.deleteButton.setOnClickListener {
            checklistManager.removeItem(checklist, item.id)
            notifyItemRemoved(position)
            onItemChanged()
        }
    }

    override fun getItemCount(): Int = checklist.items.size

    fun moveItem(from: Int, to: Int) {
        checklistManager.moveItem(checklist, from, to)
        notifyItemMoved(from, to)
    }
}

/**
 * ItemTouchHelper callback for drag and drop reordering
 */
class ChecklistItemTouchCallback(
    private val adapter: ChecklistAdapter
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Handle swipe to delete if needed
    }
}
