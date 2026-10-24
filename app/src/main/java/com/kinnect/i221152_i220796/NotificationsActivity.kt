package com.kinnect.i221152_i220796

import android.os.Bundle

/** Screen 18: Notifications (PDF page 18). */
class NotificationsActivity : BaseTabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setupTabs(3, "Notifications")
    }
}