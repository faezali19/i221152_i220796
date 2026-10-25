package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** UI-only navigation. Intent extras carry the selected display identity, not account data. */
class ChatActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // Aisha remains the default example from PDF page 21.
        val personName = intent.getStringExtra("person_name") ?: "Aisha Khan"
        val personInitials = intent.getStringExtra("person_initials") ?: "AK"
        findViewById<TextView>(R.id.chatPersonName1).text = personName
        findViewById<TextView>(R.id.chatPersonName2).text = personName
        findViewById<TextView>(R.id.chatPersonInitials1).text = personInitials
        findViewById<TextView>(R.id.chatPersonInitials2).text = personInitials
        findViewById<TextView>(R.id.chatPersonInitials3).text = personInitials
        findViewById<TextView>(R.id.chatPersonInitials4).text = personInitials
        findViewById<TextView>(R.id.chatReplyPerson).text = "You replied to ${personName.substringBefore(' ')}"
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCall).setOnClickListener {
            // PDF 22 explicitly shows Ammi / AM. Keep that fixed visual example.
            startActivity(Intent(this, VoiceCallActivity::class.java))
        }
        // Conversation bubbles remain sample UI, not person-specific message history.

    }
}
