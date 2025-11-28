package com.officesuite.app.writing

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ChecklistManager
 */
class ChecklistManagerTest {

    private lateinit var checklistManager: ChecklistManager

    @Before
    fun setup() {
        checklistManager = ChecklistManager()
    }

    @Test
    fun `createChecklist creates empty checklist with title`() {
        val checklist = checklistManager.createChecklist("My Tasks")
        
        assertEquals("My Tasks", checklist.title)
        assertTrue(checklist.items.isEmpty())
        assertTrue(checklist.id.isNotEmpty())
    }

    @Test
    fun `addItem adds item to checklist`() {
        val checklist = checklistManager.createChecklist("Tasks")
        
        val item = checklistManager.addItem(checklist, "Task 1")
        
        assertEquals(1, checklist.items.size)
        assertEquals("Task 1", item.text)
        assertFalse(item.isChecked)
        assertEquals(ChecklistManager.Priority.NORMAL, item.priority)
    }

    @Test
    fun `addItem with priority sets correct priority`() {
        val checklist = checklistManager.createChecklist("Tasks")
        
        val item = checklistManager.addItem(checklist, "Urgent Task", ChecklistManager.Priority.URGENT)
        
        assertEquals(ChecklistManager.Priority.URGENT, item.priority)
    }

    @Test
    fun `removeItem removes item from checklist`() {
        val checklist = checklistManager.createChecklist("Tasks")
        val item = checklistManager.addItem(checklist, "Task 1")
        
        checklistManager.removeItem(checklist, item.id)
        
        assertTrue(checklist.items.isEmpty())
    }

    @Test
    fun `toggleItem changes checked state`() {
        val checklist = checklistManager.createChecklist("Tasks")
        val item = checklistManager.addItem(checklist, "Task 1")
        
        assertFalse(item.isChecked)
        
        val result = checklistManager.toggleItem(checklist, item.id)
        
        assertTrue(result)
        assertTrue(item.isChecked)
        
        val result2 = checklistManager.toggleItem(checklist, item.id)
        
        assertFalse(result2)
        assertFalse(item.isChecked)
    }

    @Test
    fun `toggleItem returns false for non-existent item`() {
        val checklist = checklistManager.createChecklist("Tasks")
        
        val result = checklistManager.toggleItem(checklist, "non-existent-id")
        
        assertFalse(result)
    }

    @Test
    fun `moveItem reorders items correctly`() {
        val checklist = checklistManager.createChecklist("Tasks")
        checklistManager.addItem(checklist, "Task 1")
        checklistManager.addItem(checklist, "Task 2")
        checklistManager.addItem(checklist, "Task 3")
        
        checklistManager.moveItem(checklist, 2, 0)
        
        assertEquals("Task 3", checklist.items[0].text)
        assertEquals("Task 1", checklist.items[1].text)
        assertEquals("Task 2", checklist.items[2].text)
    }

    @Test
    fun `getProgress returns correct percentage`() {
        val checklist = checklistManager.createChecklist("Tasks")
        checklistManager.addItem(checklist, "Task 1")
        checklistManager.addItem(checklist, "Task 2")
        val item3 = checklistManager.addItem(checklist, "Task 3")
        val item4 = checklistManager.addItem(checklist, "Task 4")
        
        item3.isChecked = true
        item4.isChecked = true
        
        val progress = checklistManager.getProgress(checklist)
        
        assertEquals(0.5f, progress, 0.01f) // 2 of 4 = 50%
    }

    @Test
    fun `getProgress returns 0 for empty checklist`() {
        val checklist = checklistManager.createChecklist("Tasks")
        
        val progress = checklistManager.getProgress(checklist)
        
        assertEquals(0f, progress, 0.01f)
    }

    @Test
    fun `getCompletedCount returns correct count`() {
        val checklist = checklistManager.createChecklist("Tasks")
        val item1 = checklistManager.addItem(checklist, "Task 1")
        checklistManager.addItem(checklist, "Task 2")
        val item3 = checklistManager.addItem(checklist, "Task 3")
        
        item1.isChecked = true
        item3.isChecked = true
        
        assertEquals(2, checklistManager.getCompletedCount(checklist))
    }

    @Test
    fun `getPendingCount returns correct count`() {
        val checklist = checklistManager.createChecklist("Tasks")
        val item1 = checklistManager.addItem(checklist, "Task 1")
        checklistManager.addItem(checklist, "Task 2")
        checklistManager.addItem(checklist, "Task 3")
        
        item1.isChecked = true
        
        assertEquals(2, checklistManager.getPendingCount(checklist))
    }

    @Test
    fun `sortItems by priority sorts correctly`() {
        val checklist = checklistManager.createChecklist("Tasks")
        checklistManager.addItem(checklist, "Low Task", ChecklistManager.Priority.LOW)
        checklistManager.addItem(checklist, "Urgent Task", ChecklistManager.Priority.URGENT)
        checklistManager.addItem(checklist, "Normal Task", ChecklistManager.Priority.NORMAL)
        
        checklistManager.sortItems(checklist, ChecklistManager.SortBy.PRIORITY_DESC)
        
        assertEquals(ChecklistManager.Priority.URGENT, checklist.items[0].priority)
        assertEquals(ChecklistManager.Priority.NORMAL, checklist.items[1].priority)
        assertEquals(ChecklistManager.Priority.LOW, checklist.items[2].priority)
    }

    @Test
    fun `sortItems alphabetically sorts correctly`() {
        val checklist = checklistManager.createChecklist("Tasks")
        checklistManager.addItem(checklist, "Zebra task")
        checklistManager.addItem(checklist, "Apple task")
        checklistManager.addItem(checklist, "Banana task")
        
        checklistManager.sortItems(checklist, ChecklistManager.SortBy.ALPHABETICAL)
        
        assertEquals("Apple task", checklist.items[0].text)
        assertEquals("Banana task", checklist.items[1].text)
        assertEquals("Zebra task", checklist.items[2].text)
    }

    @Test
    fun `exportToMarkdown creates valid markdown`() {
        val checklist = checklistManager.createChecklist("My Tasks")
        val item1 = checklistManager.addItem(checklist, "Complete task 1")
        checklistManager.addItem(checklist, "Pending task 2")
        checklistManager.addItem(checklist, "Urgent task", ChecklistManager.Priority.URGENT)
        
        item1.isChecked = true
        
        val markdown = checklistManager.exportToMarkdown(checklist)
        
        assertTrue(markdown.contains("# My Tasks"))
        assertTrue(markdown.contains("[x] Complete task 1"))
        assertTrue(markdown.contains("[ ] Pending task 2"))
        assertTrue(markdown.contains("🔴")) // Urgent priority indicator
        assertTrue(markdown.contains("Progress:"))
    }

    @Test
    fun `exportToText creates valid plain text`() {
        val checklist = checklistManager.createChecklist("My Tasks")
        val item1 = checklistManager.addItem(checklist, "Task 1")
        checklistManager.addItem(checklist, "Task 2")
        
        item1.isChecked = true
        
        val text = checklistManager.exportToText(checklist)
        
        assertTrue(text.contains("My Tasks"))
        assertTrue(text.contains("[✓] Task 1"))
        assertTrue(text.contains("[ ] Task 2"))
        assertTrue(text.contains("Progress:"))
    }

    @Test
    fun `parseFromMarkdown creates checklist from markdown`() {
        val markdown = """
            # Shopping List
            - [x] Buy milk
            - [ ] Get bread
            - [ ] Pick up eggs 🔴
        """.trimIndent()
        
        val checklist = checklistManager.parseFromMarkdown(markdown)
        
        assertNotNull(checklist)
        assertEquals("Shopping List", checklist?.title)
        assertEquals(3, checklist?.items?.size)
        assertTrue(checklist?.items?.get(0)?.isChecked == true)
        assertFalse(checklist?.items?.get(1)?.isChecked == true)
        assertEquals(ChecklistManager.Priority.URGENT, checklist?.items?.get(2)?.priority)
    }

    @Test
    fun `parseFromMarkdown returns null for invalid markdown`() {
        val invalidMarkdown = "Just some text without a title or items"
        
        val checklist = checklistManager.parseFromMarkdown(invalidMarkdown)
        
        assertNull(checklist)
    }

    @Test
    fun `lastModified updates when items change`() {
        val checklist = checklistManager.createChecklist("Tasks")
        val originalModified = checklist.lastModified
        
        Thread.sleep(10) // Small delay to ensure time difference
        
        checklistManager.addItem(checklist, "New Task")
        
        assertTrue(checklist.lastModified >= originalModified)
    }
}
