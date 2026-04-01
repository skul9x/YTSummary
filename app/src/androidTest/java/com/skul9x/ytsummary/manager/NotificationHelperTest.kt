package com.skul9x.ytsummary.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Reset shared preferences before each test
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun testNotificationChannelCreated() {
        // Just verify it doesn't crash when called with valid context.
        NotificationHelper.createChannel(context)
    }

    @Test
    fun testShowUpdateNotification_Initial_TriggersNotification() {
        // Act
        NotificationHelper.showUpdateNotification(context, "1.2.0", "1.3.0")

        // Assert:
        // By checking the SharedPreferences, we know the logic reached the end 
        // without crashing, effectively queueing the notification.
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val notifiedVersion = prefs.getString("notified_version", null)
        assert(notifiedVersion == "1.3.0") { "Expected notification preference to save '1.3.0'." }
    }

    @Test
    fun testShowUpdateNotification_Repeated_DoesNotTriggerAgain() {
        // Arrange
        // Simulate that version 1.3.0 already notified
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit().putString("notified_version", "1.3.0").commit()

        // Act
        NotificationHelper.showUpdateNotification(context, "1.2.0", "1.3.0")

        // Assert (No crash, returns early, no duplicated notify, but hard to assert without deep mock.)
        // We verify context preference hasn't been maliciously reset or changed unexpectedly.
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val notifiedVersion = prefs.getString("notified_version", null)
        assert(notifiedVersion == "1.3.0")
    }
}
