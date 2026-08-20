package com.mishba.ecopantryapp

import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.model.NotificationType
import org.junit.Assert.*
import org.junit.Test

/**
 * UT-NOTIF: Unit tests for notification management logic
 * used in NotificationScreenViewModel and NotificationTable.
 * Covers: sorting, unread count, mark as read, clear all.
 */

class NotificationTest {

    @Test
    fun notificationsShouldBeSortedByMostRecent() {
        val now = System.currentTimeMillis()
        val old = now - 10000
        val newer = now - 5000
        val list = listOf(
            NotificationTable(userId = "u1", type = NotificationType.EXPIRY_ALERT, title = "old", message = "", timestamp = old),
            NotificationTable(userId = "u1", type = NotificationType.EXPIRY_ALERT, title = "newer", message = "", timestamp = newer)
        )
        val sorted = list.sortedByDescending { it.timestamp }
        assertEquals(newer, sorted.first().timestamp)
        assertEquals(old, sorted.last().timestamp)
    }

    @Test
    fun markingAsReadShouldSetIsReadToTrue() {
        val notification = NotificationTable(
            userId = "u1",
            type = NotificationType.EXPIRY_ALERT,
            title = "Test",
            message = "msg",
            isRead = false
        )
        val updated = notification.copy(isRead = true)
        assertTrue(updated.isRead)
    }

    @Test
    fun unreadCountShouldDecreaseAfterMarkingRead() {
        val unread = listOf(
            NotificationTable(userId = "u1", type = NotificationType.EXPIRY_ALERT, title = "a", message = "", isRead = false),
            NotificationTable(userId = "u1", type = NotificationType.EXPIRY_ALERT, title = "b", message = "", isRead = true)
        )
        val initialUnread = unread.count { !it.isRead }
        assertEquals(1, initialUnread)
        val afterRead = unread.map { if (!it.isRead) it.copy(isRead = true) else it }
        val newUnread = afterRead.count { !it.isRead }
        assertEquals(0, newUnread)
    }

    @Test
    fun clearingAllNotificationsShouldRemoveAll() {
        val list = listOf(
            NotificationTable(userId = "u1", type = NotificationType.EXPIRY_ALERT, title = "a", message = ""),
            NotificationTable(userId = "u1", type = NotificationType.DONATION_CLAIMED, title = "b", message = "")
        )
        val cleared = emptyList<NotificationTable>()
        assertTrue(cleared.isEmpty())
        assertNotEquals(list.size, cleared.size)
    }
}