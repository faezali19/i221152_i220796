package com.kinnect.i221152_i220796

import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

/**
 * Shared logic for the five main tab screens (Home, Friends, Marketplace,
 * Notifications, Menu). Each of those screens extends this class and calls setupTabs().
 */
open class BaseTabActivity : LiveActivity() {

    private val tabIds = intArrayOf(R.id.tabHome, R.id.tabFriends, R.id.tabMarket, R.id.tabNotif, R.id.tabMenu)
    private val iconIds = intArrayOf(R.id.icHome, R.id.icFriends, R.id.icMarket, R.id.icNotif, R.id.icMenu)
    private val lineIds = intArrayOf(R.id.ulHome, R.id.ulFriends, R.id.ulMarket, R.id.ulNotif, R.id.ulMenu)

    /** Which screen each tab opens. Add the others as we build those screens. */
    private fun targetFor(index: Int): Class<*>? = when (index) {
        0 -> HomeActivity::class.java
        1 -> FriendsActivity::class.java
        2 -> MarketplaceActivity::class.java
        3 -> NotificationsActivity::class.java
        4 -> MenuActivity::class.java
        else -> null
    }

    /**
     * Highlights the active tab and wires the clicks.
     * activeTab: 0 Home, 1 Friends, 2 Marketplace, 3 Notifications, 4 Menu.
     * title: heading text. Null keeps the teal "kinnect" logo text (Home).
     */
    protected fun setupTabs(activeTab: Int, title: String? = null) {
        // Header buttons shared by all five tab screens
        findViewById<View>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<View>(R.id.btnMessenger).setOnClickListener {
            startActivity(Intent(this, ChatsActivity::class.java))
        }

        if (title != null) {
            val tv = findViewById<TextView>(R.id.tvTitle)
            tv.text = title
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        }

        for (i in tabIds.indices) {
            val active = i == activeTab
            val color = ContextCompat.getColor(this, if (active) R.color.primary else R.color.text_dark)
            ImageViewCompat.setImageTintList(
                findViewById<ImageView>(iconIds[i]), ColorStateList.valueOf(color)
            )
            findViewById<View>(lineIds[i]).visibility = if (active) View.VISIBLE else View.INVISIBLE

            findViewById<View>(tabIds[i]).setOnClickListener {
                if (i != activeTab) openTab(i, activeTab)
            }
        }
    }

    private fun openTab(index: Int, currentTab: Int) {
        val target = targetFor(index) ?: return
        val intent = Intent(this, target)
        if (index == 0) {
            // Going to Home: clear everything above it so Back never loops between tabs
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        // Replace the current tab (unless it is Home) so the stack is always Home -> one tab
        if (currentTab != 0) finish()
        overridePendingTransition(0, 0) // no slide animation between tabs
    }
}